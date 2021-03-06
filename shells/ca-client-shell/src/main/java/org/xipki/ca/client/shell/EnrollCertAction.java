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

package org.xipki.ca.client.shell;

import java.io.File;
import java.security.cert.X509Certificate;

import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.support.completers.FileCompleter;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.bouncycastle.asn1.crmf.CertRequest;
import org.bouncycastle.asn1.crmf.POPOSigningKey;
import org.bouncycastle.asn1.crmf.ProofOfPossession;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.crmf.ProofOfPossessionSigningKeyBuilder;
import org.xipki.ca.client.api.CertifiedKeyPairOrError;
import org.xipki.ca.client.api.EnrollCertResult;
import org.xipki.ca.client.api.dto.EnrollCertRequest;
import org.xipki.ca.client.api.dto.EnrollCertRequestEntry;
import org.xipki.ca.client.api.dto.EnrollCertRequest.Type;
import org.xipki.security.ConcurrentBagEntrySigner;
import org.xipki.security.ConcurrentContentSigner;
import org.xipki.security.SignatureAlgoControl;
import org.xipki.shell.CmdFailure;
import org.xipki.shell.IllegalCmdParamException;
import org.xipki.shell.completer.DerPemCompleter;
import org.xipki.util.ObjectCreationException;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public abstract class EnrollCertAction extends EnrollAction {

  @Option(name = "--cmpreq-type",
      description = "CMP request type (ir for Initialization Request,\n"
          + "cr for Certification Request, and ccr for Cross-Certification Request)")
  @Completion(value = StringsCompleter.class, values = {"ir", "cr", "ccr"})
  private String cmpreqType = "cr";

  @Option(name = "--hash", description = "hash algorithm name for the POPO computation")
  protected String hashAlgo = "SHA256";

  @Option(name = "--outform", description = "output format of the certificate")
  @Completion(DerPemCompleter.class)
  private String outform = "der";

  @Option(name = "--out", aliases = "-o", required = true,
      description = "where to save the certificate")
  @Completion(FileCompleter.class)
  private String outputFile;

  @Option(name = "--rsa-mgf1",
      description = "whether to use the RSAPSS MGF1 for the POPO computation\n"
          + "(only applied to RSA key)")
  private Boolean rsaMgf1 = Boolean.FALSE;

  @Option(name = "--dsa-plain",
      description = "whether to use the Plain DSA for the POPO computation\n"
          + "(only applied to DSA and ECDSA key)")
  private Boolean dsaPlain = Boolean.FALSE;

  @Option(name = "--gm",
      description = "whether to use the chinese GM algorithm for the POPO computation\n"
          + "(only applied to EC key with GM curves)")
  private Boolean gm = Boolean.FALSE;

  protected SignatureAlgoControl getSignatureAlgoControl() {
    return new SignatureAlgoControl(rsaMgf1, dsaPlain, gm);
  }

  /**
   * TODO.
   * @param signatureAlgoControl
   *          Signature algorithm control. Must not be {@code null}.
   */
  protected abstract ConcurrentContentSigner getSigner() throws ObjectCreationException;

  @Override
  protected SubjectPublicKeyInfo getPublicKey() throws Exception {
    ConcurrentContentSigner signer = getSigner();
    X509CertificateHolder ssCert = signer.getBcCertificate();
    return ssCert.getSubjectPublicKeyInfo();
  }

  @Override
  protected EnrollCertRequestEntry buildEnrollCertRequestEntry(String id, String profile,
      CertRequest certRequest) throws Exception {
    ConcurrentContentSigner signer = getSigner();

    ProofOfPossessionSigningKeyBuilder popoBuilder =
        new ProofOfPossessionSigningKeyBuilder(certRequest);
    ConcurrentBagEntrySigner signer0 = signer.borrowSigner();
    POPOSigningKey popoSk;
    try {
      popoSk = popoBuilder.build(signer0.value());
    } finally {
      signer.requiteSigner(signer0);
    }

    ProofOfPossession popo = new ProofOfPossession(popoSk);
    return new EnrollCertRequestEntry(id, profile, certRequest, popo);
  }

  @Override
  protected Object execute0() throws Exception {
    EnrollCertResult result = enroll();

    X509Certificate cert = null;
    if (result != null) {
      String id = result.getAllIds().iterator().next();
      CertifiedKeyPairOrError certOrError = result.getCertOrError(id);
      cert = (X509Certificate) certOrError.getCertificate();
    }

    if (cert == null) {
      throw new CmdFailure("no certificate received from the server");
    }

    File certFile = new File(outputFile);
    saveVerbose("saved certificate to file", certFile,
        derPemEncodeCert(cert.getEncoded(), outform));

    return null;
  } // method execute0

  @Override
  protected Type getCmpReqType() throws Exception {
    if ("cr".equalsIgnoreCase(cmpreqType)) {
      return EnrollCertRequest.Type.CERT_REQ;
    } else if ("ir".equalsIgnoreCase(cmpreqType)) {
      return EnrollCertRequest.Type.INIT_REQ;
    } else if ("ccr".equalsIgnoreCase(cmpreqType)) {
      return EnrollCertRequest.Type.CROSS_CERT_REQ;
    } else {
      throw new IllegalCmdParamException("invalid cmpreq-type " + cmpreqType);
    }
  }

}
