/*
 *
 * Copyright (c) 2013 - 2018 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xipki.security.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchProviderException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERUniversalString;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.DirectoryString;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xipki.security.FpIdCalculator;
import org.xipki.security.KeyUsage;
import org.xipki.security.ObjectIdentifiers;
import org.xipki.security.exception.BadInputException;
import org.xipki.util.Base64;
import org.xipki.util.CollectionUtil;
import org.xipki.util.CompareUtil;
import org.xipki.util.ConfPairs;
import org.xipki.util.Hex;
import org.xipki.util.IoUtil;
import org.xipki.util.ParamUtil;
import org.xipki.util.StringUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class X509Util {
  private static final Logger LOG = LoggerFactory.getLogger(X509Util.class);

  private static final byte[] BEGIN_PEM = "-----BEGIN".getBytes();

  private static final byte[] END_PEM = "-----END".getBytes();

  private static final byte[] PEM_SEP = "-----".getBytes();

  private static CertificateFactory certFact;

  private static Object certFactLock = new Object();

  private X509Util() {
  }

  public static String getCommonName(X500Principal name) {
    ParamUtil.requireNonNull("name", name);
    return getCommonName(X500Name.getInstance(name.getEncoded()));
  }

  public static String getCommonName(X500Name name) {
    ParamUtil.requireNonNull("name", name);
    RDN[] rdns = name.getRDNs(ObjectIdentifiers.DN_CN);
    if (rdns != null && rdns.length > 0) {
      RDN rdn = rdns[0];
      AttributeTypeAndValue atv = null;
      if (rdn.isMultiValued()) {
        for (AttributeTypeAndValue m : rdn.getTypesAndValues()) {
          if (m.getType().equals(ObjectIdentifiers.DN_CN)) {
            atv = m;
            break;
          }
        }
      } else {
        atv = rdn.getFirst();
      }
      return (atv == null) ? null : rdnValueToString(atv.getValue());
    }
    return null;
  }

  public static X500Name reverse(X500Name name) {
    ParamUtil.requireNonNull("name", name);
    RDN[] orig = name.getRDNs();
    final int n = orig.length;
    RDN[] newRdn = new RDN[n];
    for (int i = 0; i < n; i++) {
      newRdn[i] = orig[n - 1 - i];
    }
    return new X500Name(newRdn);
  }

  public static X509Certificate parseCert(File file) throws IOException, CertificateException {
    ParamUtil.requireNonNull("file", file);
    FileInputStream in = new FileInputStream(IoUtil.expandFilepath(file));
    try {
      return parseCert(in);
    } finally {
      in.close();
    }
  }

  public static X509Certificate parseCert(InputStream certStream)
      throws IOException, CertificateException {
    ParamUtil.requireNonNull("certStream", certStream);
    return parseCert(IoUtil.read(certStream));
  }

  public static X509Certificate parseCert(byte[] certBytes) throws CertificateException {
    ParamUtil.requireNonNull("certBytes", certBytes);
    X509Certificate cert = (X509Certificate) getCertFactory().generateCertificate(
        new ByteArrayInputStream(toDerEncoded(certBytes)));
    if (cert == null) {
      throw new CertificateEncodingException("the given one is not a valid X.509 certificate");
    }
    return cert;
  }

  public static org.bouncycastle.asn1.x509.Certificate parseBcCert(File file)
      throws IOException, CertificateException {
    ParamUtil.requireNonNull("file", file);
    FileInputStream in = new FileInputStream(IoUtil.expandFilepath(file));
    try {
      return parseBcCert(in);
    } finally {
      in.close();
    }
  }

  public static org.bouncycastle.asn1.x509.Certificate parseBcCert(InputStream certStream)
      throws IOException, CertificateException {
    ParamUtil.requireNonNull("certStream", certStream);
    return parseBcCert(IoUtil.read(certStream));
  }

  public static org.bouncycastle.asn1.x509.Certificate parseBcCert(byte[] certBytes)
      throws CertificateException {
    ParamUtil.requireNonNull("certBytes", certBytes);

    try {
      return org.bouncycastle.asn1.x509.Certificate.getInstance(toDerEncoded(certBytes));
    } catch (IllegalArgumentException ex) {
      throw new CertificateEncodingException("the given one is not a valid X.509 certificate");
    }
  }

  public static CertificationRequest parseCsr(File file) throws IOException {
    ParamUtil.requireNonNull("file", file);
    FileInputStream in = new FileInputStream(IoUtil.expandFilepath(file));
    try {
      return parseCsr(in);
    } finally {
      in.close();
    }
  }

  public static CertificationRequest parseCsr(InputStream csrStream) throws IOException {
    ParamUtil.requireNonNull("csrStream", csrStream);
    return parseCsr(IoUtil.read(csrStream));
  }

  public static CertificationRequest parseCsr(byte[] csrBytes) {
    ParamUtil.requireNonNull("csrBytes", csrBytes);
    return CertificationRequest.getInstance(toDerEncoded(csrBytes));
  }

  public static byte[] toDerEncoded(byte[] bytes) {
    final int len = bytes.length;

    if (len > 23) {
      // check if PEM encoded
      if (CompareUtil.areEqual(bytes, 0, BEGIN_PEM, 0, BEGIN_PEM.length)) {
        int base64Start = -1;
        int base64End = -1;

        for (int i = BEGIN_PEM.length + 1; i < len; i++) {
          if (CompareUtil.areEqual(bytes, i, PEM_SEP, 0, PEM_SEP.length)) {
            base64Start = i + PEM_SEP.length;
            break;
          }
        }

        if (bytes[base64Start] == '\n') {
          base64Start++;
        }

        for (int i = len - END_PEM.length - 6; i > 0; i--) {
          if (CompareUtil.areEqual(bytes, i, END_PEM, 0, END_PEM.length)) {
            base64End = i - 1;
            break;
          }
        }

        if (bytes[base64End - 1] == '\r') {
          base64End--;
        }

        byte[] base64Bytes = new byte[base64End - base64Start + 1];
        System.arraycopy(bytes, base64Start, base64Bytes, 0, base64Bytes.length);
        return Base64.decode(base64Bytes);
      }
    }

    // check whether base64 encoded
    if (Base64.containsOnlyBase64Chars(bytes, 0, 10)) {
      return Base64.decode(bytes);
    }

    return bytes;
  }

  private static CertificateFactory getCertFactory() throws CertificateException {
    synchronized (certFactLock) {
      if (certFact == null) {
        try {
          certFact = CertificateFactory.getInstance("X.509", "BC");
        } catch (NoSuchProviderException ex) {
          throw new CertificateException("NoSuchProviderException: " + ex.getMessage());
        }
      }
      return certFact;
    }
  }

  public static String toPemCert(X509Certificate cert) throws CertificateException {
    ParamUtil.requireNonNull("cert", cert);
    return StringUtil.concat("-----BEGIN CERTIFICATE-----\n",
        Base64.encodeToString(cert.getEncoded(), true),
        "\n-----END CERTIFICATE-----");
  }

  public static X509Certificate toX509Cert(org.bouncycastle.asn1.x509.Certificate asn1Cert)
      throws CertificateException {
    byte[] encodedCert;
    try {
      encodedCert = asn1Cert.getEncoded();
    } catch (IOException ex) {
      throw new CertificateEncodingException("could not get encoded certificate", ex);
    }
    return parseCert(encodedCert);
  }

  public static X509CRL toX509Crl(CertificateList asn1CertList)
      throws CertificateException, CRLException {
    byte[] encodedCrl;
    try {
      encodedCrl = asn1CertList.getEncoded();
    } catch (IOException ex) {
      throw new CRLException("could not get encoded CRL", ex);
    }
    return parseCrl(encodedCrl);
  }

  public static X509CRL parseCrl(File file)
      throws IOException, CertificateException, CRLException {
    ParamUtil.requireNonNull("file", file);
    return parseCrl(new FileInputStream(IoUtil.expandFilepath(file)));
  }

  public static X509CRL parseCrl(byte[] encodedCrl) throws CertificateException, CRLException {
    ParamUtil.requireNonNull("encodedCrl", encodedCrl);
    return parseCrl(new ByteArrayInputStream(toDerEncoded(encodedCrl)));
  }

  public static X509CRL parseCrl(InputStream crlStream) throws CertificateException, CRLException {
    ParamUtil.requireNonNull("crlStream", crlStream);
    X509CRL crl = (X509CRL) getCertFactory().generateCRL(crlStream);
    if (crl == null) {
      throw new CRLException("the given one is not a valid X.509 CRL");
    }
    return crl;
  }

  public static String getRfc4519Name(X500Principal name) {
    ParamUtil.requireNonNull("name", name);
    return getRfc4519Name(X500Name.getInstance(name.getEncoded()));
  }

  public static String getRfc4519Name(X500Name name) {
    ParamUtil.requireNonNull("name", name);
    return RFC4519Style.INSTANCE.toString(name);
  }

  /**
   * First canonicalized the name, and then compute the SHA-1 finger-print over the
   * canonicalized subject string.
   * @param prin The name
   * @return the fingerprint of the canonicalized name
   */
  public static long fpCanonicalizedName(X500Principal prin) {
    ParamUtil.requireNonNull("prin", prin);
    X500Name x500Name = X500Name.getInstance(prin.getEncoded());
    return fpCanonicalizedName(x500Name);
  }

  public static long fpCanonicalizedName(X500Name name) {
    ParamUtil.requireNonNull("name", name);
    String canonicalizedName = canonicalizName(name);
    byte[] encoded;
    try {
      encoded = canonicalizedName.getBytes("UTF-8");
    } catch (UnsupportedEncodingException ex) {
      encoded = canonicalizedName.getBytes();
    }
    return FpIdCalculator.hash(encoded);
  }

  public static String canonicalizName(X500Name name) {
    ParamUtil.requireNonNull("name", name);
    ASN1ObjectIdentifier[] tmpTypes = name.getAttributeTypes();
    int len = tmpTypes.length;
    List<String> types = new ArrayList<>(len);
    for (ASN1ObjectIdentifier type : tmpTypes) {
      types.add(type.getId());
    }

    Collections.sort(types);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      String type = types.get(i);
      if (i > 0) {
        sb.append(",");
      }
      sb.append(type).append("=");
      RDN[] rdns = name.getRDNs(new ASN1ObjectIdentifier(type));

      List<String> values = new ArrayList<>(1);
      for (int j = 0; j < rdns.length; j++) {
        RDN rdn = rdns[j];
        if (rdn.isMultiValued()) {
          AttributeTypeAndValue[] atvs = rdn.getTypesAndValues();
          for (AttributeTypeAndValue atv : atvs) {
            if (type.equals(atv.getType().getId())) {
              String textValue = IETFUtils.valueToString(atv.getValue()).toLowerCase();
              values.add(textValue);
            }
          }
        } else {
          String textValue = IETFUtils.valueToString(rdn.getFirst().getValue()).toLowerCase();
          values.add(textValue);
        }
      } // end for(j)

      sb.append(values.get(0));

      final int n2 = values.size();
      if (n2 > 1) {
        for (int j = 1; j < n2; j++) {
          sb.append(";").append(values.get(j));
        }
      }
    } // end for(i)

    return sb.toString();
  } // method canonicalizName

  public static byte[] extractSki(X509Certificate cert) throws CertificateEncodingException {
    byte[] extValue = getCoreExtValue(cert, Extension.subjectKeyIdentifier);
    if (extValue == null) {
      return null;
    }

    try {
      return ASN1OctetString.getInstance(extValue).getOctets();
    } catch (IllegalArgumentException ex) {
      throw new CertificateEncodingException(ex.getMessage());
    }
  }

  public static byte[] extractSki(org.bouncycastle.asn1.x509.Certificate cert)
      throws CertificateEncodingException {
    ParamUtil.requireNonNull("cert", cert);
    Extension encodedSkiValue = cert.getTBSCertificate().getExtensions().getExtension(
        Extension.subjectKeyIdentifier);
    if (encodedSkiValue == null) {
      return null;
    }

    try {
      return ASN1OctetString.getInstance(encodedSkiValue.getParsedValue()).getOctets();
    } catch (IllegalArgumentException ex) {
      throw new CertificateEncodingException("invalid extension SubjectKeyIdentifier: "
          + ex.getMessage());
    }
  }

  public static byte[] extractAki(X509Certificate cert) throws CertificateEncodingException {
    byte[] extValue = getCoreExtValue(cert, Extension.authorityKeyIdentifier);
    if (extValue == null) {
      return null;
    }

    try {
      AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(extValue);
      return aki.getKeyIdentifier();
    } catch (IllegalArgumentException ex) {
      throw new CertificateEncodingException("invalid extension AuthorityKeyIdentifier: "
    + ex.getMessage());
    }
  }

  public static byte[] extractAki(org.bouncycastle.asn1.x509.Certificate cert)
      throws CertificateEncodingException {
    ParamUtil.requireNonNull("cert", cert);
    try {
      AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.fromExtensions(
          cert.getTBSCertificate().getExtensions());
      return (aki == null) ? null : aki.getKeyIdentifier();
    } catch (IllegalArgumentException ex) {
      throw new CertificateEncodingException("invalid extension AuthorityKeyIdentifier: "
          + ex.getMessage());
    }
  }

  public static String rdnValueToString(ASN1Encodable value) {
    ParamUtil.requireNonNull("value", value);
    if (value instanceof ASN1String && !(value instanceof DERUniversalString)) {
      return ((ASN1String) value).getString();
    } else {
      try {
        return "#" + Hex.encode(
            value.toASN1Primitive().getEncoded(ASN1Encoding.DER));
      } catch (IOException ex) {
        throw new IllegalArgumentException("other value has no encoded form");
      }
    }
  }

  public static org.bouncycastle.asn1.x509.KeyUsage createKeyUsage(Set<KeyUsage> usages) {
    if (CollectionUtil.isEmpty(usages)) {
      return null;
    }

    int usage = 0;
    for (KeyUsage keyUsage : usages) {
      usage |= keyUsage.getBcUsage();
    }

    return new org.bouncycastle.asn1.x509.KeyUsage(usage);
  }

  public static ExtendedKeyUsage createExtendedUsage(Collection<ASN1ObjectIdentifier> usages) {
    if (CollectionUtil.isEmpty(usages)) {
      return null;
    }

    List<ASN1ObjectIdentifier> list = new ArrayList<>(usages);
    List<ASN1ObjectIdentifier> sortedUsages = sortOidList(list);
    KeyPurposeId[] kps = new KeyPurposeId[sortedUsages.size()];

    int idx = 0;
    for (ASN1ObjectIdentifier oid : sortedUsages) {
      kps[idx++] = KeyPurposeId.getInstance(oid);
    }

    return new ExtendedKeyUsage(kps);
  }

  // sort the list and remove duplicated OID.
  public static List<ASN1ObjectIdentifier> sortOidList(List<ASN1ObjectIdentifier> oids) {
    ParamUtil.requireNonNull("oids", oids);
    List<String> list = new ArrayList<>(oids.size());
    for (ASN1ObjectIdentifier m : oids) {
      list.add(m.getId());
    }
    Collections.sort(list);

    List<ASN1ObjectIdentifier> sorted = new ArrayList<>(oids.size());
    for (String m : list) {
      for (ASN1ObjectIdentifier n : oids) {
        if (m.equals(n.getId()) && !sorted.contains(n)) {
          sorted.add(n);
        }
      }
    }
    return sorted;
  }

  public static boolean hasKeyusage(X509Certificate cert, KeyUsage usage) {
    ParamUtil.requireNonNull("cert", cert);
    boolean[] keyusage = cert.getKeyUsage();
    if (keyusage != null && keyusage.length > usage.getBit()) {
      return keyusage[usage.getBit()];
    }
    return false;
  }

  public static byte[] getCoreExtValue(X509Certificate cert, ASN1ObjectIdentifier type)
      throws CertificateEncodingException {
    ParamUtil.requireNonNull("cert", cert);
    ParamUtil.requireNonNull("type", type);
    byte[] fullExtValue = cert.getExtensionValue(type.getId());
    if (fullExtValue == null) {
      return null;
    }
    try {
      return ASN1OctetString.getInstance(fullExtValue).getOctets();
    } catch (IllegalArgumentException ex) {
      throw new CertificateEncodingException("invalid extension " + type.getId() + ": "
          + ex.getMessage());
    }
  }

  public static byte[] getCoreExtValue(X509AttributeCertificateHolder cert,
      ASN1ObjectIdentifier type) throws CertificateEncodingException {
    ParamUtil.requireNonNull("cert", cert);
    ParamUtil.requireNonNull("type", type);
    Extension ext = cert.getExtension(type);
    if (ext == null) {
      return null;
    }

    return ext.getExtnValue().getOctets();
  }

  /**
   * Build the certificate path. Cross certificate will not be considered.
   * @param cert certificate for which the certificate path will be built
   * @param certs collection of certificates.
   * @return the certificate path
   */
  public static X509Certificate[] buildCertPath(X509Certificate cert,
      Set<? extends Certificate> certs) {
    ParamUtil.requireNonNull("cert", cert);
    List<X509Certificate> certChain = new LinkedList<>();
    certChain.add(cert);
    try {
      if (certs != null && !isSelfSigned(cert)) {
        while (true) {
          X509Certificate caCert = getCaCertOf(certChain.get(certChain.size() - 1), certs);
          if (caCert == null) {
            break;
          }
          certChain.add(caCert);
          if (isSelfSigned(caCert)) {
            // reaches root self-signed certificate
            break;
          }
        }
      }
    } catch (CertificateEncodingException ex) {
      LOG.warn("CertificateEncodingException: {}", ex.getMessage());
    }

    final int n = certChain.size();
    int len = n;
    if (n > 1) {
      for (int i = 1; i < n; i++) {
        int pathLen = certChain.get(i).getBasicConstraints();
        if (pathLen < 0 || pathLen < i) {
          len = i;
          break;
        }
      }
    } // end for

    if (len == n) {
      return certChain.toArray(new X509Certificate[0]);
    } else {
      X509Certificate[] ret = new X509Certificate[len];
      for (int i = 0; i < len; i++) {
        ret[i] = certChain.get(i);
      }
      return ret;
    }
  } // method buildCertPath

  private static X509Certificate getCaCertOf(X509Certificate cert,
      Set<? extends Certificate> caCerts) throws CertificateEncodingException {
    ParamUtil.requireNonNull("cert", cert);
    if (isSelfSigned(cert)) {
      return null;
    }

    for (Certificate caCert : caCerts) {
      if (!(caCert instanceof X509Certificate)) {
        continue;
      }

      X509Certificate x509CaCert = (X509Certificate) caCert;
      if (!issues(x509CaCert, cert)) {
        continue;
      }

      try {
        cert.verify(x509CaCert.getPublicKey());
        return x509CaCert;
      } catch (Exception ex) {
        LOG.warn("could not verify certificate: {}", ex.getMessage());
      }
    }

    return null;
  }

  public static boolean isSelfSigned(X509Certificate cert) throws CertificateEncodingException {
    ParamUtil.requireNonNull("cert", cert);
    boolean equals = cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    if (equals) {
      byte[] ski = extractSki(cert);
      byte[] aki = extractAki(cert);
      if (ski != null && aki != null) {
        equals = Arrays.equals(ski, aki);
      }
    }
    return equals;
  }

  public static boolean issues(X509Certificate issuerCert, X509Certificate cert)
      throws CertificateEncodingException {
    ParamUtil.requireNonNull("issuerCert", issuerCert);
    ParamUtil.requireNonNull("cert", cert);

    boolean issues = issuerCert.getSubjectX500Principal().equals(
        cert.getIssuerX500Principal());
    if (issues) {
      byte[] ski = extractSki(issuerCert);
      byte[] aki = extractAki(cert);
      if (ski != null) {
        issues = Arrays.equals(ski, aki);
      }
    }

    if (issues) {
      long issuerNotBefore = issuerCert.getNotBefore().getTime();
      long issuerNotAfter = issuerCert.getNotAfter().getTime();
      long notBefore = cert.getNotBefore().getTime();
      issues = notBefore <= issuerNotAfter && notBefore >= issuerNotBefore;
    }

    return issues;
  }

  public static boolean issues(org.bouncycastle.asn1.x509.Certificate issuerCert,
      org.bouncycastle.asn1.x509.Certificate cert) throws CertificateEncodingException {
    ParamUtil.requireNonNull("issuerCert", issuerCert);
    ParamUtil.requireNonNull("cert", cert);

    boolean issues = issuerCert.getSubject().equals(cert.getIssuer());
    if (issues) {
      byte[] ski = extractSki(issuerCert);
      byte[] aki = extractAki(cert);
      if (ski != null) {
        issues = Arrays.equals(ski, aki);
      }
    }

    if (issues) {
      long issuerNotBefore = issuerCert.getStartDate().getDate().getTime();
      long issuerNotAfter = issuerCert.getEndDate().getDate().getTime();
      long notBefore = cert.getStartDate().getDate().getTime();
      issues = notBefore <= issuerNotAfter && notBefore >= issuerNotBefore;
    }

    return issues;
  }

  public static SubjectPublicKeyInfo toRfc3279Style(SubjectPublicKeyInfo publicKeyInfo)
      throws InvalidKeySpecException {
    ParamUtil.requireNonNull("publicKeyInfo", publicKeyInfo);
    ASN1ObjectIdentifier algOid = publicKeyInfo.getAlgorithm().getAlgorithm();
    ASN1Encodable keyParameters = publicKeyInfo.getAlgorithm().getParameters();

    if (PKCSObjectIdentifiers.rsaEncryption.equals(algOid)) {
      if (DERNull.INSTANCE.equals(keyParameters)) {
        return publicKeyInfo;
      } else {
        AlgorithmIdentifier keyAlgId = new AlgorithmIdentifier(algOid, DERNull.INSTANCE);
        return new SubjectPublicKeyInfo(keyAlgId, publicKeyInfo.getPublicKeyData().getBytes());
      }
    } else if (X9ObjectIdentifiers.id_dsa.equals(algOid)) {
      if (keyParameters == null) {
        return publicKeyInfo;
      } else if (DERNull.INSTANCE.equals(keyParameters)) {
        AlgorithmIdentifier keyAlgId = new AlgorithmIdentifier(algOid);
        return new SubjectPublicKeyInfo(keyAlgId, publicKeyInfo.getPublicKeyData().getBytes());
      } else {
        try {
          DSAParameter.getInstance(keyParameters);
        } catch (IllegalArgumentException ex) {
          throw new InvalidKeySpecException("keyParameters is not null and Dss-Parms");
        }
        return publicKeyInfo;
      }
    } else if (X9ObjectIdentifiers.id_ecPublicKey.equals(algOid)) {
      if (keyParameters == null) {
        throw new InvalidKeySpecException("keyParameters is not an OBJECT IDENTIFIER");
      }
      try {
        ASN1ObjectIdentifier.getInstance(keyParameters);
      } catch (IllegalArgumentException ex) {
        throw new InvalidKeySpecException("keyParameters is not an OBJECT IDENTIFIER");
      }
      return publicKeyInfo;
    } else {
      return publicKeyInfo;
    }
  }

  public static String cutText(String text, int maxLen) {
    ParamUtil.requireNonNull("text", text);
    if (text.length() <= maxLen) {
      return text;
    }
    return StringUtil.concat(text.substring(0, maxLen - 13), "...skipped...");
  }

  public static String cutX500Name(X500Name name, int maxLen) {
    String text = getRfc4519Name(name);
    return cutText(text, maxLen);
  }

  public static String cutX500Name(X500Principal name, int maxLen) {
    String text = getRfc4519Name(name);
    return cutText(text, maxLen);
  }

  public static Extension createExtnSubjectAltName(List<String> taggedValues, boolean critical)
      throws BadInputException {
    GeneralNames names = createGeneralNames(taggedValues);
    if (names == null) {
      return null;
    }

    try {
      return new Extension(Extension.subjectAlternativeName, critical, names.getEncoded());
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public static Extension createExtnSubjectInfoAccess(List<String> accessMethodAndLocations,
      boolean critical) throws BadInputException {
    if (CollectionUtil.isEmpty(accessMethodAndLocations)) {
      return null;
    }

    ASN1EncodableVector vector = new ASN1EncodableVector();
    for (String accessMethodAndLocation : accessMethodAndLocations) {
      vector.add(createAccessDescription(accessMethodAndLocation));
    }
    ASN1Sequence seq = new DERSequence(vector);
    try {
      return new Extension(Extension.subjectInfoAccess, critical, seq.getEncoded());
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public static AccessDescription createAccessDescription(String accessMethodAndLocation)
      throws BadInputException {
    ParamUtil.requireNonNull("accessMethodAndLocation", accessMethodAndLocation);
    ConfPairs pairs;
    try {
      pairs = new ConfPairs(accessMethodAndLocation);
    } catch (IllegalArgumentException ex) {
      throw new BadInputException("invalid accessMethodAndLocation " + accessMethodAndLocation);
    }

    Set<String> oids = pairs.names();
    if (oids == null || oids.size() != 1) {
      throw new BadInputException("invalid accessMethodAndLocation " + accessMethodAndLocation);
    }

    String accessMethodS = oids.iterator().next();
    String taggedValue = pairs.value(accessMethodS);
    ASN1ObjectIdentifier accessMethod = new ASN1ObjectIdentifier(accessMethodS);

    GeneralName location = createGeneralName(taggedValue);
    return new AccessDescription(accessMethod, location);
  }

  public static GeneralNames createGeneralNames(List<String> taggedValues)
      throws BadInputException {
    if (CollectionUtil.isEmpty(taggedValues)) {
      return null;
    }

    int len = taggedValues.size();
    GeneralName[] names = new GeneralName[len];
    for (int i = 0; i < len; i++) {
      names[i] = createGeneralName(taggedValues.get(i));
    }
    return new GeneralNames(names);
  }

  /**
  * Creates {@link GeneralName} from the tagged value.
  * @param taggedValue [tag]value, and the value for tags otherName and ediPartyName is
  *     type=value.
  * @return the created {@link GeneralName}
  * @throws BadInputException
  *         if the {@code taggedValue} is invalid.
  */
  public static GeneralName createGeneralName(String taggedValue) throws BadInputException {
    ParamUtil.requireNonBlank("taggedValue", taggedValue);

    int tag = -1;
    String value = null;
    if (taggedValue.charAt(0) == '[') {
      int idx = taggedValue.indexOf(']', 1);
      if (idx > 1 && idx < taggedValue.length() - 1) {
        String tagS = taggedValue.substring(1, idx);
        try {
          tag = Integer.parseInt(tagS);
          value = taggedValue.substring(idx + 1);
        } catch (NumberFormatException ex) {
          throw new BadInputException("invalid tag '" + tagS + "'");
        }
      }
    }

    if (tag == -1) {
      throw new BadInputException("invalid taggedValue " + taggedValue);
    }

    switch (tag) {
      case GeneralName.otherName:
        if (value == null) {
          throw new BadInputException("invalid otherName: no value specified");
        }

        int idxSep = value.indexOf("=");
        if (idxSep == -1 || idxSep == 0 || idxSep == value.length() - 1) {
          throw new BadInputException("invalid otherName " + value);
        }
        String otherTypeOid = value.substring(0, idxSep);
        ASN1ObjectIdentifier type = new ASN1ObjectIdentifier(otherTypeOid);
        String otherValue = value.substring(idxSep + 1);
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(type);
        vector.add(new DERTaggedObject(true, 0, new DERUTF8String(otherValue)));
        DERSequence seq = new DERSequence(vector);
        return new GeneralName(GeneralName.otherName, seq);
      case GeneralName.rfc822Name:
        return new GeneralName(tag, value);
      case GeneralName.dNSName:
        return new GeneralName(tag, value);
      case GeneralName.directoryName:
        X500Name x500Name = reverse(new X500Name(value));
        return new GeneralName(GeneralName.directoryName, x500Name);
      case GeneralName.ediPartyName:
        if (value == null) {
          throw new BadInputException("invalid ediPartyName: no value specified");
        }
        idxSep = value.indexOf("=");
        if (idxSep == -1 || idxSep == value.length() - 1) {
          throw new BadInputException("invalid ediPartyName " + value);
        }
        String nameAssigner = (idxSep == 0) ? null : value.substring(0, idxSep);
        String partyName = value.substring(idxSep + 1);
        vector = new ASN1EncodableVector();
        if (nameAssigner != null) {
          vector.add(new DERTaggedObject(false, 0, new DirectoryString(nameAssigner)));
        }
        vector.add(new DERTaggedObject(false, 1, new DirectoryString(partyName)));
        seq = new DERSequence(vector);
        return new GeneralName(GeneralName.ediPartyName, seq);
      case GeneralName.uniformResourceIdentifier:
        return new GeneralName(tag, value);
      case GeneralName.iPAddress:
        return new GeneralName(tag, value);
      case GeneralName.registeredID:
        return new GeneralName(tag, value);
      default:
        throw new RuntimeException("unsupported tag " + tag);
    } // end switch (tag)
  } // method createGeneralName

}
