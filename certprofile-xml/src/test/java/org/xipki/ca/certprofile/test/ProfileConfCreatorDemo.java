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

package org.xipki.ca.certprofile.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.SchemaFactory;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.w3c.dom.Element;
import org.xipki.ca.api.profile.CertLevel;
import org.xipki.ca.api.profile.X509CertVersion;
import org.xipki.ca.certprofile.xml.XmlCertprofileUtil;
import org.xipki.ca.certprofile.xml.jaxb.AdditionalInformation;
import org.xipki.ca.certprofile.xml.jaxb.AdmissionSyntax;
import org.xipki.ca.certprofile.xml.jaxb.AdmissionsType;
import org.xipki.ca.certprofile.xml.jaxb.AlgorithmType;
import org.xipki.ca.certprofile.xml.jaxb.AnyType;
import org.xipki.ca.certprofile.xml.jaxb.AuthorityInfoAccess;
import org.xipki.ca.certprofile.xml.jaxb.AuthorityKeyIdentifier;
import org.xipki.ca.certprofile.xml.jaxb.AuthorizationTemplate;
import org.xipki.ca.certprofile.xml.jaxb.Base64BinaryWithDescType;
import org.xipki.ca.certprofile.xml.jaxb.BasicConstraints;
import org.xipki.ca.certprofile.xml.jaxb.BiometricInfo;
import org.xipki.ca.certprofile.xml.jaxb.BiometricTypeType;
import org.xipki.ca.certprofile.xml.jaxb.CertificatePolicies;
import org.xipki.ca.certprofile.xml.jaxb.CertificatePolicyInformationType;
import org.xipki.ca.certprofile.xml.jaxb.ConstantExtValue;
import org.xipki.ca.certprofile.xml.jaxb.ConstantValueType;
import org.xipki.ca.certprofile.xml.jaxb.DirectoryStringType;
import org.xipki.ca.certprofile.xml.jaxb.DsaParameters;
import org.xipki.ca.certprofile.xml.jaxb.EcParameters;
import org.xipki.ca.certprofile.xml.jaxb.EcParameters.Curves;
import org.xipki.ca.certprofile.xml.jaxb.EcParameters.PointEncodings;
import org.xipki.ca.certprofile.xml.jaxb.ExtendedKeyUsage;
import org.xipki.ca.certprofile.xml.jaxb.ExtendedKeyUsage.Usage;
import org.xipki.ca.certprofile.xml.jaxb.ExtensionType;
import org.xipki.ca.certprofile.xml.jaxb.ExtensionValueType;
import org.xipki.ca.certprofile.xml.jaxb.ExtensionsType;
import org.xipki.ca.certprofile.xml.jaxb.GeneralNameType;
import org.xipki.ca.certprofile.xml.jaxb.GeneralNameType.OtherName;
import org.xipki.ca.certprofile.xml.jaxb.GeneralSubtreeBaseType;
import org.xipki.ca.certprofile.xml.jaxb.GeneralSubtreesType;
import org.xipki.ca.certprofile.xml.jaxb.InhibitAnyPolicy;
import org.xipki.ca.certprofile.xml.jaxb.IntWithDescType;
import org.xipki.ca.certprofile.xml.jaxb.KeyParametersType;
import org.xipki.ca.certprofile.xml.jaxb.KeyUsage;
import org.xipki.ca.certprofile.xml.jaxb.KeyUsageEnum;
import org.xipki.ca.certprofile.xml.jaxb.NameConstraints;
import org.xipki.ca.certprofile.xml.jaxb.NamingAuthorityType;
import org.xipki.ca.certprofile.xml.jaxb.ObjectFactory;
import org.xipki.ca.certprofile.xml.jaxb.OidWithDescType;
import org.xipki.ca.certprofile.xml.jaxb.PdsLocationType;
import org.xipki.ca.certprofile.xml.jaxb.PdsLocationsType;
import org.xipki.ca.certprofile.xml.jaxb.PolicyConstraints;
import org.xipki.ca.certprofile.xml.jaxb.PolicyIdMappingType;
import org.xipki.ca.certprofile.xml.jaxb.PolicyMappings;
import org.xipki.ca.certprofile.xml.jaxb.PrivateKeyUsagePeriod;
import org.xipki.ca.certprofile.xml.jaxb.ProfessionInfoType;
import org.xipki.ca.certprofile.xml.jaxb.ProfessionInfoType.RegistrationNumber;
import org.xipki.ca.certprofile.xml.jaxb.QcEuLimitValueType;
import org.xipki.ca.certprofile.xml.jaxb.QcStatementType;
import org.xipki.ca.certprofile.xml.jaxb.QcStatementValueType;
import org.xipki.ca.certprofile.xml.jaxb.QcStatements;
import org.xipki.ca.certprofile.xml.jaxb.Range2Type;
import org.xipki.ca.certprofile.xml.jaxb.RangeType;
import org.xipki.ca.certprofile.xml.jaxb.RangesType;
import org.xipki.ca.certprofile.xml.jaxb.RdnType;
import org.xipki.ca.certprofile.xml.jaxb.Restriction;
import org.xipki.ca.certprofile.xml.jaxb.RsaParameters;
import org.xipki.ca.certprofile.xml.jaxb.SmimeCapabilities;
import org.xipki.ca.certprofile.xml.jaxb.SmimeCapability;
import org.xipki.ca.certprofile.xml.jaxb.SubjectAltName;
import org.xipki.ca.certprofile.xml.jaxb.SubjectDirectoryAttributs;
import org.xipki.ca.certprofile.xml.jaxb.SubjectInfoAccess;
import org.xipki.ca.certprofile.xml.jaxb.SubjectToSubjectAltNameType;
import org.xipki.ca.certprofile.xml.jaxb.SubjectToSubjectAltNameType.Target;
import org.xipki.ca.certprofile.xml.jaxb.SubjectToSubjectAltNamesType;
import org.xipki.ca.certprofile.xml.jaxb.TlsFeature;
import org.xipki.ca.certprofile.xml.jaxb.TripleState;
import org.xipki.ca.certprofile.xml.jaxb.UsageType;
import org.xipki.ca.certprofile.xml.jaxb.ValidityModel;
import org.xipki.ca.certprofile.xml.jaxb.X509ProfileType;
import org.xipki.ca.certprofile.xml.jaxb.X509ProfileType.KeyAlgorithms;
import org.xipki.ca.certprofile.xml.jaxb.X509ProfileType.SignatureAlgorithms;
import org.xipki.ca.certprofile.xml.jaxb.X509ProfileType.Subject;
import org.xipki.security.HashAlgo;
import org.xipki.security.ObjectIdentifiers;
import org.xipki.security.TlsExtensionType;
import org.xipki.security.util.AlgorithmUtil;
import org.xipki.util.ParamUtil;
import org.xipki.util.StringUtil;
import org.xipki.util.XmlUtil;
import org.xml.sax.SAXException;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class ProfileConfCreatorDemo {

  private static class ExampleDescription extends AnyType {

    ExampleDescription(Element appInfo) {
      setAny(appInfo);
    }

  } // class ExampleDescription

  private static final String REGEX_FQDN =
      "(?=^.{1,254}$)(^(?:(?!\\d+\\.|-)[a-zA-Z0-9_\\-]{1,63}(?<!-)\\.?)+(?:[a-zA-Z]{2,})$)";

  private static final String REGEX_SN = "[\\d]{1,}";

  private static final Set<ASN1ObjectIdentifier> REQUEST_EXTENSIONS;

  static {
    REQUEST_EXTENSIONS = new HashSet<>();
    REQUEST_EXTENSIONS.add(Extension.keyUsage);
    REQUEST_EXTENSIONS.add(Extension.extendedKeyUsage);
    REQUEST_EXTENSIONS.add(Extension.subjectAlternativeName);
    REQUEST_EXTENSIONS.add(Extension.subjectInfoAccess);
  }

  private ProfileConfCreatorDemo() {
  }

  public static void main(String[] args) {
    try {
      Marshaller ms = JAXBContext.newInstance(ObjectFactory.class).createMarshaller();
      final SchemaFactory schemaFact = SchemaFactory.newInstance(
          javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
      URL url = ProfileConfCreatorDemo.class.getResource("/xsd/certprofile.xsd");
      ms.setSchema(schemaFact.newSchema(url));
      ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      ms.setProperty("com.sun.xml.internal.bind.indentString", "  ");

      int year = Calendar.getInstance().get(Calendar.YEAR);
      ms.setProperty("com.sun.xml.internal.bind.xmlHeaders",
          "\n"
          + "<!--\n"
          + "  Copyright (c) 2013 - " + year + " Lijun Liao\n"
          + "\n"
          + "  Licensed under the Apache License, Version 2.0 (the \"License\");\n"
          + "  you may not use this file except in compliance with the License.\n"
          + "  You may obtain a copy of the License at\n"
          + "\n"
          + "  http://www.apache.org/licenses/LICENSE-2.0\n"
          + "\n"
          + "  Unless required by applicable law or agreed to in writing, software\n"
          + "  distributed under the License is distributed on an \"AS IS\" BASIS,\n"
          + "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
          + "  See the License for the specific language governing permissions and\n"
          + "  limitations under the License.\n"
          + "-->");

      X509ProfileType profile = certprofileRootCa();
      marshall(ms, profile, "certprofile-rootca.xml");

      profile = certprofileCross();
      marshall(ms, profile, "certprofile-cross.xml");

      profile = certprofileSubCa();
      marshall(ms, profile, "certprofile-subca.xml");

      profile = certprofileSubCaComplex();
      marshall(ms, profile, "certprofile-subca-complex.xml");

      profile = certprofileOcsp();
      marshall(ms, profile, "certprofile-ocsp.xml");

      profile = certprofileScep();
      marshall(ms, profile, "certprofile-scep.xml");

      profile = certprofileEeComplex();
      marshall(ms, profile, "certprofile-ee-complex.xml");

      profile = certprofileQc();
      marshall(ms, profile, "certprofile-qc.xml");

      profile = certprofileTls();
      marshall(ms, profile, "certprofile-tls.xml");

      profile = certprofileTlsC();
      marshall(ms, profile, "certprofile-tls-c.xml");

      profile = certprofileTlsWithIncSerial();
      marshall(ms, profile, "certprofile-tls-inc-sn.xml");

      profile = certprofileMultipleOus();
      marshall(ms, profile, "certprofile-multiple-ous.xml");

      profile = certprofileMultipleValuedRdn();
      marshall(ms, profile, "certprofile-multi-valued-rdn.xml");

      profile = certprofileMaxTime();
      marshall(ms, profile, "certprofile-max-time.xml");

      profile = certprofileExtended();
      marshall(ms, profile, "certprofile-extended.xml");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  } // method main

  private static void marshall(Marshaller marshaller, X509ProfileType profile, String filename)
      throws Exception {
    File file = new File("tmp", filename);
    file.getParentFile().mkdirs();
    JAXBElement<X509ProfileType> root = new ObjectFactory().createX509Profile(profile);
    FileOutputStream out = new FileOutputStream(file);
    try {
      marshaller.marshal(root, out);
    } catch (JAXBException ex) {
      throw XmlUtil.convert(ex);
    } finally {
      out.close();
    }
    System.out.println("Generated certprofile in " + filename);
  } // method marshal

  private static X509ProfileType certprofileRootCa() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile rootca", CertLevel.RootCA, "10y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();

    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.KEY_CERT_SIGN},
        new KeyUsageEnum[]{KeyUsageEnum.CRL_SIGN});
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileRootCa

  private static X509ProfileType certprofileCross() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile cross", CertLevel.SubCA, "10y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();

    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(false);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.KEY_CERT_SIGN}, null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileCross

  private static X509ProfileType certprofileSubCa() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile subca", CertLevel.SubCA, "8y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();

    // Extensions - controls
    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = createBasicConstraints(1);
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(false);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.KEY_CERT_SIGN},
        new KeyUsageEnum[]{KeyUsageEnum.CRL_SIGN});
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileSubCa

  private static X509ProfileType certprofileSubCaComplex() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile subca-complex (with most extensions)",
        CertLevel.SubCA, "8y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1, null, "PREFIX ", " SUFFIX"));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();

    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = createBasicConstraints(1);
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(false);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.KEY_CERT_SIGN},
        new KeyUsageEnum[]{KeyUsageEnum.CRL_SIGN});
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Certificate Policies
    extensionValue = createCertificatePolicies(new ASN1ObjectIdentifier("1.2.3.4.5"),
        new ASN1ObjectIdentifier("2.4.3.2.1"));
    list.add(createExtension(Extension.certificatePolicies, true, false, extensionValue));

    // Policy Mappings
    PolicyMappings policyMappings = new PolicyMappings();
    policyMappings.getMapping().add(createPolicyIdMapping(new ASN1ObjectIdentifier("1.1.1.1.1"),
        new ASN1ObjectIdentifier("2.1.1.1.1")));
    policyMappings.getMapping().add(createPolicyIdMapping(new ASN1ObjectIdentifier("1.1.1.1.2"),
        new ASN1ObjectIdentifier("2.1.1.1.2")));
    extensionValue = createExtensionValueType(policyMappings);
    list.add(createExtension(Extension.policyMappings, true, true, extensionValue));

    // Policy Constraints
    PolicyConstraints policyConstraints = createPolicyConstraints(2, 2);
    extensionValue = createExtensionValueType(policyConstraints);
    list.add(createExtension(Extension.policyConstraints, true, true, extensionValue));

    // Name Constrains
    NameConstraints nameConstraints = createNameConstraints();
    extensionValue = createExtensionValueType(nameConstraints);
    list.add(createExtension(Extension.nameConstraints, true, true, extensionValue));

    // Inhibit anyPolicy
    InhibitAnyPolicy inhibitAnyPolicy = createInhibitAnyPolicy(1);
    extensionValue = createExtensionValueType(inhibitAnyPolicy);
    list.add(createExtension(Extension.inhibitAnyPolicy, true, true, extensionValue));

    // SubjectAltName
    SubjectAltName subjectAltNameMode = new SubjectAltName();

    OtherName otherName = new OtherName();
    otherName.getType().add(createOidType(ObjectIdentifiers.DN_O));
    subjectAltNameMode.setOtherName(otherName);
    subjectAltNameMode.setRfc822Name("");
    subjectAltNameMode.setDnsName("");
    subjectAltNameMode.setDirectoryName("");
    subjectAltNameMode.setEdiPartyName("");
    subjectAltNameMode.setUri("");
    subjectAltNameMode.setIpAddress("");
    subjectAltNameMode.setRegisteredId("");

    extensionValue = createExtensionValueType(subjectAltNameMode);
    list.add(createExtension(Extension.subjectAlternativeName, true, false, extensionValue));

    // SubjectInfoAccess
    SubjectInfoAccess subjectInfoAccessMode = new SubjectInfoAccess();
    SubjectInfoAccess.Access access = new SubjectInfoAccess.Access();
    subjectInfoAccessMode.getAccess().add(access);

    access.setAccessMethod(createOidType(ObjectIdentifiers.id_ad_caRepository));

    GeneralNameType accessLocation = new GeneralNameType();
    access.setAccessLocation(accessLocation);
    accessLocation.setDirectoryName("");
    accessLocation.setUri("");

    extensionValue = createExtensionValueType(subjectInfoAccessMode);
    list.add(createExtension(Extension.subjectInfoAccess, true, false, extensionValue));

    // Custom Extension
    ASN1ObjectIdentifier customExtensionOid = new ASN1ObjectIdentifier("1.2.3.4");
    extensionValue = createConstantExtValue(DERNull.INSTANCE.getEncoded(), "DER Null");
    list.add(createExtension(customExtensionOid, true, false, extensionValue,
        "custom extension 1"));

    return profile;
  } // method certprofileSubCaComplex

  private static X509ProfileType certprofileOcsp() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile ocsp", CertLevel.EndEntity, "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(true);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_organizationIdentifier, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();
    List<ExtensionType> list = extensions.getExtension();

    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));
    list.add(createExtension(ObjectIdentifiers.id_extension_pkix_ocsp_nocheck, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.CONTENT_COMMITMENT}, null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_OCSPSigning}, null);
    list.add(createExtension(Extension.extendedKeyUsage, true, false, extensionValue));

    return profile;
  } // method certprofileOcsp

  private static X509ProfileType certprofileScep() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile scep", CertLevel.EndEntity, "5y", false);

    profile.setKeyAlgorithms(createRSAKeyAlgorithms());

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();
    List<ExtensionType> list = extensions.getExtension();

    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.KEY_ENCIPHERMENT}, null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileScep

  private static X509ProfileType certprofileTls() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile tls", CertLevel.EndEntity, "5y", true);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1, REGEX_FQDN, null, null));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();

    // SubjectToSubjectAltName
    extensions.setSubjectToSubjectAltNames(new SubjectToSubjectAltNamesType());
    SubjectToSubjectAltNameType s2sType = new SubjectToSubjectAltNameType();
    extensions.getSubjectToSubjectAltNames().getSubjectToSubjectAltName().add(s2sType);
    s2sType.setSource(createOidType(ObjectIdentifiers.DN_CN));
    s2sType.setTarget(new Target());
    s2sType.getTarget().setDnsName("");

    // Extensions - controls
    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - SubjectAltNames
    SubjectAltName subjectAltNameMode = new SubjectAltName();
    subjectAltNameMode.setDnsName("");
    subjectAltNameMode.setIpAddress("");
    ExtensionValueType extensionValue = createExtensionValueType(subjectAltNameMode);
    list.add(createExtension(Extension.subjectAlternativeName, true, false, extensionValue));

    // Extensions - basicConstraints
    extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.DATA_ENCIPHERMENT,
          KeyUsageEnum.KEY_ENCIPHERMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_serverAuth},
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_clientAuth});
    list.add(createExtension(Extension.extendedKeyUsage, true, false, extensionValue));

    // Extensions - tlsFeature
    extensionValue = createTlsFeature(
        new TlsExtensionType[]{TlsExtensionType.STATUS_REQUEST,
          TlsExtensionType.CLIENT_CERTIFICATE_URL});
    list.add(createExtension(ObjectIdentifiers.id_pe_tlsfeature, true, true, extensionValue));

    // Extensions - SMIMECapabilities
    extensionValue = createSmimeCapabilities();
    list.add(createExtension(ObjectIdentifiers.id_smimeCapabilities, true, false, extensionValue));

    return profile;
  } // method certprofileTls

  private static X509ProfileType certprofileTlsC() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile tls-c", CertLevel.EndEntity, "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();
    List<ExtensionType> list = extensions.getExtension();

    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.DATA_ENCIPHERMENT,
          KeyUsageEnum.KEY_ENCIPHERMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_clientAuth}, null);
    list.add(createExtension(Extension.extendedKeyUsage, true, false, extensionValue));

    return profile;
  } // method certprofileTlsC

  private static X509ProfileType certprofileTlsWithIncSerial() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile tls-inc-sn "
        + "(serial number will be added automatically)", CertLevel.EndEntity, "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(true);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1, REGEX_FQDN, null, null));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();

    // Extensions - controls
    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.DATA_ENCIPHERMENT,
          KeyUsageEnum.KEY_ENCIPHERMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_serverAuth},
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_clientAuth});
    list.add(createExtension(Extension.extendedKeyUsage, true, false, extensionValue));

    return profile;
  } // method certprofileTlsWithIncSerial

  private static X509ProfileType certprofileMultipleOus() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile multiple-ous",
        CertLevel.EndEntity, "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));

    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 2, 2));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();
    List<ExtensionType> list = extensions.getExtension();

    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.CONTENT_COMMITMENT}, null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileMultipleOus

  /*
   * O and OU in one RDN
   */
  private static X509ProfileType certprofileMultipleValuedRdn() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile multiple-valued-rdn",
        CertLevel.EndEntity, "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1, null, null, null, "group1"));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 1, 1, null, null, null, "group1"));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();
    List<ExtensionType> list = extensions.getExtension();

    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(new KeyUsageEnum[]{KeyUsageEnum.CONTENT_COMMITMENT}, null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileMultipleValuedRdn

  private static X509ProfileType certprofileQc() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile qc", CertLevel.EndEntity, "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_organizationIdentifier, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();

    // Extensions - controls
    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, false, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.CONTENT_COMMITMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_timeStamping}, null);
    list.add(createExtension(Extension.extendedKeyUsage, true, true, extensionValue));

    // privateKeyUsagePeriod
    extensionValue = createPrivateKeyUsagePeriod("3y");
    list.add(createExtension(Extension.privateKeyUsagePeriod, true, false, extensionValue));

    // QcStatements
    extensionValue = createQcStatements(false);
    list.add(createExtension(Extension.qCStatements, true, false, extensionValue));

    return profile;
  } // method certprofileEeComplex

  private static X509ProfileType certprofileEeComplex() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile ee-complex", CertLevel.EndEntity,
        "5y", true);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);
    subject.setKeepRdnOrder(true);
    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_DATE_OF_BIRTH, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_POSTAL_ADDRESS, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_UNIQUE_IDENTIFIER, 1, 1));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();

    // Extensions - controls
    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, false, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.DATA_ENCIPHERMENT,
          KeyUsageEnum.KEY_ENCIPHERMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_serverAuth},
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_clientAuth});
    list.add(createExtension(Extension.extendedKeyUsage, true, false, extensionValue));

    // Extension - subjectDirectoryAttributes
    SubjectDirectoryAttributs subjectDirAttrType = new SubjectDirectoryAttributs();
    List<OidWithDescType> attrTypes = subjectDirAttrType.getType();
    attrTypes.add(createOidType(ObjectIdentifiers.DN_COUNTRY_OF_CITIZENSHIP));
    attrTypes.add(createOidType(ObjectIdentifiers.DN_COUNTRY_OF_RESIDENCE));
    attrTypes.add(createOidType(ObjectIdentifiers.DN_GENDER));
    attrTypes.add(createOidType(ObjectIdentifiers.DN_DATE_OF_BIRTH));
    attrTypes.add(createOidType(ObjectIdentifiers.DN_PLACE_OF_BIRTH));
    extensionValue = createExtensionValueType(subjectDirAttrType);
    list.add(createExtension(Extension.subjectDirectoryAttributes, true, false, extensionValue));

    // Extension - Admission
    AdmissionSyntax admissionSyntax = new AdmissionSyntax();
    admissionSyntax.setAdmissionAuthority(
        new GeneralName(new X500Name("C=DE,CN=admissionAuthority level 1")).getEncoded());

    AdmissionsType admissions = new AdmissionsType();
    admissions.setAdmissionAuthority(
        new GeneralName(new X500Name("C=DE,CN=admissionAuthority level 2")).getEncoded());

    NamingAuthorityType namingAuthorityL2 = new NamingAuthorityType();
    namingAuthorityL2.setOid(createOidType(new ASN1ObjectIdentifier("1.2.3.4.5")));
    namingAuthorityL2.setUrl("http://naming-authority-level2.example.org");
    namingAuthorityL2.setText("namingAuthrityText level 2");
    admissions.setNamingAuthority(namingAuthorityL2);

    admissionSyntax.getContentsOfAdmissions().add(admissions);

    ProfessionInfoType pi = new ProfessionInfoType();
    admissions.getProfessionInfo().add(pi);

    pi.getProfessionOid().add(createOidType(new ASN1ObjectIdentifier("1.2.3.4"), "demo oid"));
    pi.getProfessionItem().add("demo item");

    NamingAuthorityType namingAuthorityL3 = new NamingAuthorityType();
    namingAuthorityL3.setOid(createOidType(new ASN1ObjectIdentifier("1.2.3.4.5")));
    namingAuthorityL3.setUrl("http://naming-authority-level3.example.org");
    namingAuthorityL3.setText("namingAuthrityText level 3");
    pi.setNamingAuthority(namingAuthorityL3);
    pi.setAddProfessionInfo(new byte[]{1, 2, 3, 4});

    RegistrationNumber regNum = new RegistrationNumber();
    pi.setRegistrationNumber(regNum);
    regNum.setRegex("a*b");

    // check the syntax
    XmlCertprofileUtil.buildAdmissionSyntax(false, admissionSyntax);

    extensionValue = createExtensionValueType(admissionSyntax);
    list.add(createExtension(ObjectIdentifiers.id_extension_admission, true, false,
        extensionValue));

    // restriction
    extensionValue = createRestriction(DirectoryStringType.UTF_8_STRING, "demo restriction");
    list.add(createExtension(ObjectIdentifiers.id_extension_restriction, true, false,
        extensionValue));

    // additionalInformation
    extensionValue = createAdditionalInformation(DirectoryStringType.UTF_8_STRING,
        "demo additional information");
    list.add(createExtension(ObjectIdentifiers.id_extension_additionalInformation, true, false,
        extensionValue));

    // validationModel
    extensionValue = createConstantExtValue(
        new ASN1ObjectIdentifier("1.3.6.1.4.1.8301.3.5.1").getEncoded(), "chain");
    list.add(createExtension(ObjectIdentifiers.id_extension_validityModel, true, false,
        extensionValue));

    // privateKeyUsagePeriod
    extensionValue = createPrivateKeyUsagePeriod("3y");
    list.add(createExtension(Extension.privateKeyUsagePeriod, true, false, extensionValue));

    // QcStatements
    extensionValue = createQcStatements(true);
    list.add(createExtension(Extension.qCStatements, true, false, extensionValue));

    // biometricInfo
    extensionValue = createBiometricInfo();
    list.add(createExtension(Extension.biometricInfo, true, false, extensionValue));

    // authorizationTemplate
    extensionValue = createAuthorizationTemplate();
    list.add(createExtension(ObjectIdentifiers.id_xipki_ext_authorizationTemplate, true, false,
        extensionValue));

    // SubjectAltName
    SubjectAltName subjectAltNameMode = new SubjectAltName();

    OtherName otherName = new OtherName();
    otherName.getType().add(createOidType(new ASN1ObjectIdentifier("1.2.3.1"), "dummy oid 1"));
    otherName.getType().add(createOidType(new ASN1ObjectIdentifier("1.2.3.2"), "dummy oid 2"));
    subjectAltNameMode.setOtherName(otherName);
    subjectAltNameMode.setRfc822Name("");
    subjectAltNameMode.setDnsName("");
    subjectAltNameMode.setDirectoryName("");
    subjectAltNameMode.setEdiPartyName("");
    subjectAltNameMode.setUri("");
    subjectAltNameMode.setIpAddress("");
    subjectAltNameMode.setRegisteredId("");

    extensionValue = createExtensionValueType(subjectAltNameMode);
    list.add(createExtension(Extension.subjectAlternativeName, true, false, extensionValue));

    // SubjectInfoAccess
    List<ASN1ObjectIdentifier> accessMethods = new LinkedList<>();
    accessMethods.add(ObjectIdentifiers.id_ad_caRepository);
    for (int i = 0; i < 10; i++) {
      accessMethods.add(new ASN1ObjectIdentifier("2.3.4." + (i + 1)));
    }

    SubjectInfoAccess subjectInfoAccessMode = new SubjectInfoAccess();
    for (ASN1ObjectIdentifier accessMethod : accessMethods) {
      SubjectInfoAccess.Access access = new SubjectInfoAccess.Access();
      subjectInfoAccessMode.getAccess().add(access);
      access.setAccessMethod(createOidType(accessMethod));

      GeneralNameType accessLocation = new GeneralNameType();
      access.setAccessLocation(accessLocation);

      otherName = new OtherName();
      otherName.getType().add(createOidType(new ASN1ObjectIdentifier("1.2.3.1"), "dummy oid 1"));
      otherName.getType().add(createOidType(new ASN1ObjectIdentifier("1.2.3.2"), "dummy oid 2"));
      accessLocation.setOtherName(otherName);
      accessLocation.setRfc822Name("");
      accessLocation.setDnsName("");
      accessLocation.setDirectoryName("");
      accessLocation.setEdiPartyName("");
      accessLocation.setUri("");
      accessLocation.setIpAddress("");
      accessLocation.setRegisteredId("");
    }

    extensionValue = createExtensionValueType(subjectInfoAccessMode);
    list.add(createExtension(Extension.subjectInfoAccess, true, false, extensionValue));
    return profile;
  } // method certprofileEeComplex

  private static X509ProfileType certprofileMaxTime() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile max-time", CertLevel.EndEntity,
        "9999y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1, REGEX_FQDN, null, null));

    // Extensions
    ExtensionsType extensions = profile.getExtensions();
    List<ExtensionType> list = extensions.getExtension();

    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - basicConstraints
    ExtensionValueType extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.DATA_ENCIPHERMENT,
          KeyUsageEnum.KEY_ENCIPHERMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    return profile;
  } // method certprofileMaxTime

  private static X509ProfileType certprofileExtended() throws Exception {
    X509ProfileType profile = getBaseProfile("certprofile extended", CertLevel.EndEntity,
        "5y", false);

    // Subject
    Subject subject = profile.getSubject();
    subject.setIncSerialNumber(false);

    List<RdnType> rdnControls = subject.getRdn();
    rdnControls.add(createRdn(ObjectIdentifiers.DN_C, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_O, 1, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_OU, 0, 1));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_SN, 0, 1, REGEX_SN, null, null));
    rdnControls.add(createRdn(ObjectIdentifiers.DN_CN, 1, 1, REGEX_FQDN, null, null));

    // Extensions
    // Extensions - general
    ExtensionsType extensions = profile.getExtensions();

    // SubjectToSubjectAltName
    extensions.setSubjectToSubjectAltNames(new SubjectToSubjectAltNamesType());
    SubjectToSubjectAltNameType s2sType = new SubjectToSubjectAltNameType();
    extensions.getSubjectToSubjectAltNames().getSubjectToSubjectAltName().add(s2sType);
    s2sType.setSource(createOidType(ObjectIdentifiers.DN_CN));
    s2sType.setTarget(new Target());
    s2sType.getTarget().setDnsName("");

    // Extensions - controls
    List<ExtensionType> list = extensions.getExtension();
    list.add(createExtension(Extension.subjectKeyIdentifier, true, false, null));
    list.add(createExtension(Extension.cRLDistributionPoints, false, false, null));
    list.add(createExtension(Extension.freshestCRL, false, false, null));

    // Extensions - SubjectAltNames
    SubjectAltName subjectAltNameMode = new SubjectAltName();
    subjectAltNameMode.setDnsName("");
    subjectAltNameMode.setIpAddress("");
    ExtensionValueType extensionValue = createExtensionValueType(subjectAltNameMode);
    list.add(createExtension(Extension.subjectAlternativeName, true, false, extensionValue));

    // Extensions - basicConstraints
    extensionValue = null;
    list.add(createExtension(Extension.basicConstraints, true, true, extensionValue));

    // Extensions - AuthorityInfoAccess
    extensionValue = createAuthorityInfoAccess();
    list.add(createExtension(Extension.authorityInfoAccess, true, false, extensionValue));

    // Extensions - AuthorityKeyIdentifier
    extensionValue = createAuthorityKeyIdentifier(true);
    list.add(createExtension(Extension.authorityKeyIdentifier, true, false, extensionValue));

    // Extensions - keyUsage
    extensionValue = createKeyUsages(
        new KeyUsageEnum[]{KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.DATA_ENCIPHERMENT,
          KeyUsageEnum.KEY_ENCIPHERMENT},
        null);
    list.add(createExtension(Extension.keyUsage, true, true, extensionValue));

    // Extensions - extenedKeyUsage
    extensionValue = createExtendedKeyUsage(
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_serverAuth},
        new ASN1ObjectIdentifier[]{ObjectIdentifiers.id_kp_clientAuth});
    list.add(createExtension(Extension.extendedKeyUsage, true, false, extensionValue));

    // Extensions - tlsFeature
    extensionValue = createTlsFeature(
        new TlsExtensionType[]{TlsExtensionType.STATUS_REQUEST,
          TlsExtensionType.CLIENT_CERTIFICATE_URL});
    list.add(createExtension(ObjectIdentifiers.id_pe_tlsfeature, true, true, extensionValue));

    // Extensions - SMIMECapabilities
    extensionValue = createSmimeCapabilities();
    list.add(createExtension(ObjectIdentifiers.id_smimeCapabilities, true, false, extensionValue));

    // Extensions - 1.2.3.4.1 (demo-ca-extraInfo)
    list.add(createExtension(
        new ASN1ObjectIdentifier("1.2.3.4.1"), true, false, null, "demo-ca-extraInfo"));

    // Extensions - 1.2.3.4.2 (demo-other-namespace)
    String xmlBlock = "<sequence xmlns='urn:extra'>"
        + "\n          <text>aaa</text>"
        + "\n          <text>bbb</text>"
        + "\n        </sequence>";
    Element element;
    try {
      element = XmlUtil.getDocumentElment(xmlBlock.getBytes());
    } catch (IOException | SAXException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
    ExtensionValueType extnValue = new ExtensionValueType();
    extnValue.setAny(element);
    list.add(createExtension(new ASN1ObjectIdentifier("1.2.3.4.2"), true, false,
        extnValue, "demo-other-namespace"));

    return profile;
  } // method certprofileExtended

  private static RdnType createRdn(ASN1ObjectIdentifier type, int min, int max) {
    return createRdn(type, min, max, null, null, null);
  }

  private static RdnType createRdn(ASN1ObjectIdentifier type, int min, int max,
      String regex, String prefix, String suffix) {
    return createRdn(type, min, max, regex, prefix, suffix, null);
  }

  private static RdnType createRdn(ASN1ObjectIdentifier type, int min, int max,
      String regex, String prefix, String suffix, String group) {
    RdnType ret = new RdnType();
    ret.setType(createOidType(type));
    ret.setMinOccurs(min);
    ret.setMaxOccurs(max);

    if (regex != null) {
      ret.setRegex(regex);
    }

    if (StringUtil.isNotBlank(prefix)) {
      ret.setPrefix(prefix);
    }

    if (StringUtil.isNotBlank(suffix)) {
      ret.setSuffix(suffix);
    }

    if (StringUtil.isNotBlank(group)) {
      ret.setGroup(group);
    }

    return ret;
  } // method createRdn

  private static ExtensionType createExtension(ASN1ObjectIdentifier type, boolean required,
      boolean critical, ExtensionValueType extValue) {
    return createExtension(type, required, critical, extValue, null);
  }

  private static ExtensionType createExtension(ASN1ObjectIdentifier type, boolean required,
      boolean critical, ExtensionValueType extValue, String description) {
    ExtensionType ret = new ExtensionType();
    // attributes
    ret.setRequired(required);
    ret.setPermittedInRequest(REQUEST_EXTENSIONS.contains(type));
    // children
    ret.setType(createOidType(type, description));
    ret.setCritical(critical);
    ret.setValue(extValue);
    return ret;
  }

  private static ExtensionValueType createKeyUsages(KeyUsageEnum[] requiredUsages,
      KeyUsageEnum[] optionalUsages) {
    KeyUsage extValue = new KeyUsage();
    if (requiredUsages != null) {
      for (KeyUsageEnum m : requiredUsages) {
        UsageType usage = new UsageType();
        usage.setValue(m);
        usage.setRequired(true);
        extValue.getUsage().add(usage);
      }
    }
    if (optionalUsages != null) {
      for (KeyUsageEnum m : optionalUsages) {
        UsageType usage = new UsageType();
        usage.setValue(m);
        usage.setRequired(false);
        extValue.getUsage().add(usage);
      }
    }

    return createExtensionValueType(extValue);
  }

  private static ExtensionValueType createAuthorityKeyIdentifier(
      boolean includeSerialAndSerial) {

    AuthorityKeyIdentifier akiType = new AuthorityKeyIdentifier();
    akiType.setIncludeIssuerAndSerial(includeSerialAndSerial);
    return createExtensionValueType(akiType);

  }

  private static ExtensionValueType createAuthorityInfoAccess() {
    AuthorityInfoAccess extnValue = new AuthorityInfoAccess();
    extnValue.setIncludeCaIssuers(true);
    extnValue.setIncludeOcsp(true);
    return createExtensionValueType(extnValue);
  }

  private static ExtensionValueType createBasicConstraints(int pathLen) {
    BasicConstraints extValue = new BasicConstraints();
    extValue.setPathLen(pathLen);
    return createExtensionValueType(extValue);
  }

  private static ExtensionValueType createExtendedKeyUsage(
      ASN1ObjectIdentifier[] requiredUsages, ASN1ObjectIdentifier[] optionalUsages) {
    ExtendedKeyUsage extValue = new ExtendedKeyUsage();
    if (requiredUsages != null) {
      List<ASN1ObjectIdentifier> oids = Arrays.asList(requiredUsages);
      oids = sortOidList(oids);
      for (ASN1ObjectIdentifier usage : oids) {
        extValue.getUsage().add(createSingleExtKeyUsage(usage, true));
      }
    }

    if (optionalUsages != null) {
      List<ASN1ObjectIdentifier> oids = Arrays.asList(optionalUsages);
      oids = sortOidList(oids);
      for (ASN1ObjectIdentifier usage : oids) {
        extValue.getUsage().add(createSingleExtKeyUsage(usage, false));
      }
    }

    return createExtensionValueType(extValue);
  }

  private static Usage createSingleExtKeyUsage(ASN1ObjectIdentifier usage, boolean required) {
    Usage type = new Usage();
    type.setValue(usage.getId());
    type.setRequired(required);
    String desc = getDescription(usage);
    if (desc != null) {
      type.setDescription(desc);
    }
    return type;
  }

  private static ExtensionValueType createRestriction(DirectoryStringType type, String text) {
    Restriction extValue = new Restriction();
    extValue.setType(type);
    extValue.setText(text);
    return createExtensionValueType(extValue);
  }

  private static ExtensionValueType createAdditionalInformation(DirectoryStringType type,
      String text) {
    AdditionalInformation extValue = new AdditionalInformation();
    extValue.setType(type);
    extValue.setText(text);
    return createExtensionValueType(extValue);
  }

  private static ExtensionValueType createPrivateKeyUsagePeriod(String validity) {
    PrivateKeyUsagePeriod extValue = new PrivateKeyUsagePeriod();
    extValue.setValidity(validity);
    return createExtensionValueType(extValue);
  }

  private static ExtensionValueType createQcStatements(boolean requireRequestExt) {
    QcStatements extValue = new QcStatements();
    QcStatementType statement = new QcStatementType();

    // QcCompliance
    statement.setStatementId(createOidType(ObjectIdentifiers.id_etsi_qcs_QcCompliance));
    extValue.getQcStatement().add(statement);

    // QC SCD
    statement = new QcStatementType();
    statement.setStatementId(createOidType(ObjectIdentifiers.id_etsi_qcs_QcSSCD));
    extValue.getQcStatement().add(statement);

    // QC RetentionPeriod
    statement = new QcStatementType();
    statement.setStatementId(createOidType(ObjectIdentifiers.id_etsi_qcs_QcRetentionPeriod));
    QcStatementValueType statementValue = new QcStatementValueType();
    statementValue.setQcRetentionPeriod(10);
    statement.setStatementValue(statementValue);
    extValue.getQcStatement().add(statement);

    // QC LimitValue
    statement = new QcStatementType();
    statement.setStatementId(createOidType(ObjectIdentifiers.id_etsi_qcs_QcLimitValue));
    statementValue = new QcStatementValueType();

    QcEuLimitValueType euLimit = new QcEuLimitValueType();
    euLimit.setCurrency("EUR");
    Range2Type rangeAmount = new Range2Type();
    int min = 100;
    rangeAmount.setMin(min);
    rangeAmount.setMax(requireRequestExt ? 200 : min);
    euLimit.setAmount(rangeAmount);

    Range2Type rangeExponent = new Range2Type();
    min = 10;
    rangeExponent.setMin(min);
    rangeExponent.setMax(requireRequestExt ? 20 : min);
    euLimit.setExponent(rangeExponent);

    statementValue.setQcEuLimitValue(euLimit);
    statement.setStatementValue(statementValue);
    extValue.getQcStatement().add(statement);

    // QC PDS
    statement = new QcStatementType();
    statement.setStatementId(createOidType(ObjectIdentifiers.id_etsi_qcs_QcPDS));
    extValue.getQcStatement().add(statement);
    statementValue = new QcStatementValueType();
    statement.setStatementValue(statementValue);
    PdsLocationsType pdsLocations = new PdsLocationsType();
    statementValue.setPdsLocations(pdsLocations);

    PdsLocationType pdsLocation = new PdsLocationType();
    pdsLocations.getPdsLocation().add(pdsLocation);
    pdsLocation.setUrl("http://pki.example.org/pds/en");
    pdsLocation.setLanguage("en");

    pdsLocation = new PdsLocationType();
    pdsLocations.getPdsLocation().add(pdsLocation);
    pdsLocation.setUrl("http://pki.example.org/pds/de");
    pdsLocation.setLanguage("de");

    // QC Constant value
    statement = new QcStatementType();
    statement.setStatementId(createOidType(new ASN1ObjectIdentifier("1.2.3.4.5"), "dummy"));
    statementValue = new QcStatementValueType();
    ConstantValueType value = new ConstantValueType();
    try {
      value.setValue(DERNull.INSTANCE.getEncoded());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    value.setDescription("DER NULL");
    statementValue.setConstant(value);
    statement.setStatementValue(statementValue);
    extValue.getQcStatement().add(statement);

    return createExtensionValueType(extValue);
  } // method createQcStatements

  private static ExtensionValueType createBiometricInfo() {
    BiometricInfo extValue = new BiometricInfo();

    // type
    // predefined image (0)
    BiometricTypeType type = new BiometricTypeType();
    extValue.getType().add(type);

    IntWithDescType predefined = new IntWithDescType();
    predefined.setValue(0);
    predefined.setDescription("image");
    type.setPredefined(predefined);

    // predefined handwritten-signature(1)
    type = new BiometricTypeType();
    predefined = new IntWithDescType();
    predefined.setValue(1);
    predefined.setDescription("handwritten-signature");
    type.setPredefined(predefined);
    extValue.getType().add(type);

    // OID
    type = new BiometricTypeType();
    type.setOid(createOidType(new ASN1ObjectIdentifier("1.2.3.4.5.6"), "dummy biometric type"));
    extValue.getType().add(type);

    // hash algorithm
    HashAlgo[] hashAlgos = new HashAlgo[]{HashAlgo.SHA256, HashAlgo.SHA384};
    for (HashAlgo hashAlgo : hashAlgos) {
      extValue.getHashAlgorithm().add(createOidType(hashAlgo.getOid(), hashAlgo.getName()));
    }

    extValue.setIncludeSourceDataUri(TripleState.REQUIRED);
    return createExtensionValueType(extValue);
  } // method createBiometricInfo

  private static ExtensionValueType createAuthorizationTemplate() {
    AuthorizationTemplate extValue = new AuthorizationTemplate();
    extValue.setType(createOidType(new ASN1ObjectIdentifier("1.2.3.4.5"), "dummy type"));
    ConstantValueType accessRights = new ConstantValueType();
    accessRights.setDescription("dummy access rights");
    accessRights.setValue(new byte[]{1, 2, 3, 4});
    extValue.setAccessRights(accessRights);

    return createExtensionValueType(extValue);
  }

  @SuppressWarnings("unused")
  private static ExtensionValueType createValidityModel(OidWithDescType modelId) {
    ValidityModel extValue = new ValidityModel();
    extValue.setModelId(modelId);
    return createExtensionValueType(extValue);
  }

  private static ExtensionValueType createCertificatePolicies(
      ASN1ObjectIdentifier... policyOids) {
    if (policyOids == null || policyOids.length == 0) {
      return null;
    }

    CertificatePolicies extValue = new CertificatePolicies();
    List<CertificatePolicyInformationType> pis = extValue.getCertificatePolicyInformation();
    for (ASN1ObjectIdentifier oid : policyOids) {
      CertificatePolicyInformationType single = new CertificatePolicyInformationType();
      pis.add(single);
      single.setPolicyIdentifier(createOidType(oid));
    }

    return createExtensionValueType(extValue);
  }

  private static String getDescription(ASN1ObjectIdentifier oid) {
    return ObjectIdentifiers.getName(oid);
  }

  private static PolicyIdMappingType createPolicyIdMapping(
      ASN1ObjectIdentifier issuerPolicyId, ASN1ObjectIdentifier subjectPolicyId) {
    PolicyIdMappingType ret = new PolicyIdMappingType();
    ret.setIssuerDomainPolicy(createOidType(issuerPolicyId));
    ret.setSubjectDomainPolicy(createOidType(subjectPolicyId));

    return ret;
  }

  private static PolicyConstraints createPolicyConstraints(Integer inhibitPolicyMapping,
      Integer requireExplicitPolicy) {
    PolicyConstraints ret = new PolicyConstraints();
    if (inhibitPolicyMapping != null) {
      ret.setInhibitPolicyMapping(inhibitPolicyMapping);
    }

    if (requireExplicitPolicy != null) {
      ret.setRequireExplicitPolicy(requireExplicitPolicy);
    }
    return ret;
  }

  private static NameConstraints createNameConstraints() {
    NameConstraints ret = new NameConstraints();
    GeneralSubtreesType permitted = new GeneralSubtreesType();
    GeneralSubtreeBaseType single = new GeneralSubtreeBaseType();
    single.setDirectoryName("O=example organization, C=DE");
    permitted.getBase().add(single);
    ret.setPermittedSubtrees(permitted);

    GeneralSubtreesType excluded = new GeneralSubtreesType();
    single = new GeneralSubtreeBaseType();
    single.setDirectoryName("OU=bad OU, O=example organization, C=DE");
    excluded.getBase().add(single);
    ret.setExcludedSubtrees(excluded);

    return ret;
  }

  private static InhibitAnyPolicy createInhibitAnyPolicy(int skipCerts) {
    InhibitAnyPolicy ret = new InhibitAnyPolicy();
    ret.setSkipCerts(skipCerts);
    return ret;
  }

  private static OidWithDescType createOidType(ASN1ObjectIdentifier oid) {
    return createOidType(oid, null);
  }

  private static OidWithDescType createOidType(ASN1ObjectIdentifier oid, String description) {
    OidWithDescType ret = new OidWithDescType();
    ret.setValue(oid.getId());

    String desc = (description == null) ? getDescription(oid) : description;
    if (desc != null) {
      ret.setDescription(desc);
    }
    return ret;
  }

  private static ExtensionValueType createConstantExtValue(byte[] bytes, String desc) {
    ConstantExtValue extValue = new ConstantExtValue();
    extValue.setValue(bytes);
    if (StringUtil.isNotBlank(desc)) {
      extValue.setDescription(desc);
    }
    return createExtensionValueType(extValue);
  }

  private static X509ProfileType getBaseProfile(String description, CertLevel certLevel,
      String validity, boolean useMidnightNotBefore) {
    X509ProfileType profile = new X509ProfileType();

    profile.setAppInfo(createDescription(description));
    profile.setCertLevel(certLevel.toString());
    profile.setMaxSize(5000);
    profile.setVersion(X509CertVersion.v3.name());
    profile.setValidity(validity);
    profile.setNotBeforeTime(useMidnightNotBefore ? "midnight" : "current");

    profile.setSerialNumberInReq(false);

    // SignatureAlgorithms
    String[] sigHashAlgos = new String[]{"SHA3-512", "SHA3-384", "SHA3-256", "SHA3-224",
      "SHA512", "SHA384", "SHA256", "SHA1"};

    SignatureAlgorithms sigAlgosType = new SignatureAlgorithms();
    profile.setSignatureAlgorithms(sigAlgosType);

    List<String> algos = sigAlgosType.getAlgorithm();
    String[] algoPart2s = new String[]{"withRSA", "withDSA", "withECDSA", "withRSAandMGF1"};
    for (String part2 : algoPart2s) {
      for (String hashAlgo : sigHashAlgos) {
        algos.add(hashAlgo + part2);
      }
    }

    String part2 = "withPlainECDSA";
    for (String hashAlgo : sigHashAlgos) {
      if (!hashAlgo.startsWith("SHA3-")) {
        algos.add(hashAlgo + part2);
      }
    }

    algos.add("SM3withSM2");

    // Subject
    Subject subject = new Subject();
    profile.setSubject(subject);
    subject.setKeepRdnOrder(false);

    ASN1ObjectIdentifier[] curveIds = (CertLevel.EndEntity != certLevel) ? null :
      new ASN1ObjectIdentifier[] {SECObjectIdentifiers.secp256r1,
        TeleTrusTObjectIdentifiers.brainpoolP256r1, GMObjectIdentifiers.sm2p256v1};

    // Key
    profile.setKeyAlgorithms(createKeyAlgorithms(curveIds));

    // Extensions
    ExtensionsType extensions = new ExtensionsType();
    profile.setExtensions(extensions);

    return profile;
  } // method getBaseProfile

  private static KeyAlgorithms createKeyAlgorithms(ASN1ObjectIdentifier[] curveIds) {
    KeyAlgorithms ret = new KeyAlgorithms();
    List<AlgorithmType> list = ret.getAlgorithm();
    // RSA
    AlgorithmType algorithm = new AlgorithmType();
    list.add(algorithm);

    algorithm.getAlgorithm().add(createOidType(PKCSObjectIdentifiers.rsaEncryption, "RSA"));

    RsaParameters rsaParams = new RsaParameters();
    algorithm.setParameters(createKeyParametersType(rsaParams));

    RangesType ranges = new RangesType();
    rsaParams.setModulusLength(ranges);
    List<RangeType> modulusLengths = ranges.getRange();
    modulusLengths.add(createRange(1024));
    modulusLengths.add(createRange(2048));
    modulusLengths.add(createRange(3072));
    modulusLengths.add(createRange(4096));

    // DSA
    algorithm = new AlgorithmType();
    list.add(algorithm);

    algorithm.getAlgorithm().add(createOidType(X9ObjectIdentifiers.id_dsa, "DSA"));
    DsaParameters dsaParams = new DsaParameters();
    algorithm.setParameters(createKeyParametersType(dsaParams));

    ranges = new RangesType();
    dsaParams.setPLength(ranges);

    List<RangeType> plengths = ranges.getRange();
    plengths.add(createRange(1024));
    plengths.add(createRange(2048));
    plengths.add(createRange(3072));

    ranges = new RangesType();
    dsaParams.setQLength(ranges);
    List<RangeType> qlengths = ranges.getRange();
    qlengths.add(createRange(160));
    qlengths.add(createRange(224));
    qlengths.add(createRange(256));

    // EC
    algorithm = new AlgorithmType();
    list.add(algorithm);

    algorithm.getAlgorithm().add(createOidType(X9ObjectIdentifiers.id_ecPublicKey, "EC"));
    EcParameters ecParams = new EcParameters();
    algorithm.setParameters(createKeyParametersType(ecParams));

    if (curveIds != null && curveIds.length > 0) {
      Curves curves = new Curves();
      ecParams.setCurves(curves);

      for (ASN1ObjectIdentifier curveId : curveIds) {
        String name = AlgorithmUtil.getCurveName(curveId);
        curves.getCurve().add(createOidType(curveId, name));
      }
    }

    ecParams.setPointEncodings(new PointEncodings());
    final Byte unpressed = 4;
    ecParams.getPointEncodings().getPointEncoding().add(unpressed);

    return ret;
  } // method createKeyAlgorithms

  // CHECKSTYLE:SKIP
  private static KeyAlgorithms createRSAKeyAlgorithms() {
    KeyAlgorithms ret = new KeyAlgorithms();
    List<AlgorithmType> list = ret.getAlgorithm();
    AlgorithmType algorithm = new AlgorithmType();
    list.add(algorithm);

    algorithm.getAlgorithm().add(createOidType(PKCSObjectIdentifiers.rsaEncryption, "RSA"));

    RsaParameters params = new RsaParameters();
    algorithm.setParameters(createKeyParametersType(params));

    RangesType ranges = new RangesType();
    params.setModulusLength(ranges);
    List<RangeType> modulusLengths = ranges.getRange();
    modulusLengths.add(createRange(2048));
    modulusLengths.add(createRange(3072));
    modulusLengths.add(createRange(4096));

    return ret;
  }

  private static RangeType createRange(int size) {
    return createRange(size, size);
  }

  private static RangeType createRange(Integer min, Integer max) {
    if (min == null && max == null) {
      throw new IllegalArgumentException("min and max can not be both null");
    }

    RangeType range = new RangeType();
    if (min != null) {
      range.setMin(min);
    }
    if (max != null) {
      range.setMax(max);
    }
    return range;
  }

  private static AnyType createDescription(String details) {
    String str = StringUtil.concat(
        "<myDescription xmlns=\"http://example.org\">\n"
        + "      <category>cat A</category>\n"
        + "      <details>", details, "</details>\n"
        + "    </myDescription>\n");
    Element element;
    try {
      element = XmlUtil.getDocumentElment(str.getBytes());
    } catch (IOException | SAXException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
    return new ExampleDescription(element);
  }

  private static ExtensionValueType createExtensionValueType(Object object) {
    ExtensionValueType ret = new ExtensionValueType();
    ret.setAny(object);
    return ret;
  }

  private static KeyParametersType createKeyParametersType(Object object) {
    KeyParametersType ret = new KeyParametersType();
    ret.setAny(object);
    return ret;
  }

  private static ExtensionValueType createTlsFeature(TlsExtensionType[] features) {
    List<TlsExtensionType> exts = Arrays.asList(features);
    Collections.sort(exts);

    TlsFeature tlsFeature = new TlsFeature();
    for (TlsExtensionType m : exts) {
      IntWithDescType ints = new IntWithDescType();
      ints.setValue(m.getCode());
      ints.setDescription(m.getName());
      tlsFeature.getFeature().add(ints);
    }
    return createExtensionValueType(tlsFeature);
  }

  private static ExtensionValueType createSmimeCapabilities() {
    SmimeCapabilities caps = new SmimeCapabilities();

    // DES-EDE3-CBC
    SmimeCapability cap = new SmimeCapability();
    caps.getSmimeCapability().add(cap);
    cap.setCapabilityId(createOidType(new ASN1ObjectIdentifier("1.2.840.113549.3.7"),
        "DES-EDE3-CBC"));

    // RC2-CBC keysize 128
    cap = new SmimeCapability();
    caps.getSmimeCapability().add(cap);
    cap.setCapabilityId(createOidType(new ASN1ObjectIdentifier("1.2.840.113549.3.2"), "RC2-CBC"));
    cap.setParameters(new org.xipki.ca.certprofile.xml.jaxb.SmimeCapability.Parameters());
    cap.getParameters().setInteger(BigInteger.valueOf(128));

    // RC2-CBC keysize 64
    cap = new SmimeCapability();
    caps.getSmimeCapability().add(cap);
    cap.setCapabilityId(createOidType(new ASN1ObjectIdentifier("1.2.840.113549.3.2"), "RC2-CBC"));
    cap.setParameters(new org.xipki.ca.certprofile.xml.jaxb.SmimeCapability.Parameters());

    Base64BinaryWithDescType binary = new Base64BinaryWithDescType();
    try {
      binary.setValue(new ASN1Integer(64).getEncoded());
      binary.setDescription("INTEGER 64");
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }
    cap.getParameters().setBase64Binary(binary);

    return createExtensionValueType(caps);
  }

  private static List<ASN1ObjectIdentifier> sortOidList(List<ASN1ObjectIdentifier> oids) {
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

}
