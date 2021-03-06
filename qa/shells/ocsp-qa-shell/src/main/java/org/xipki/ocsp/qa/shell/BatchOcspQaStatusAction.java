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

package org.xipki.ocsp.qa.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.FileCompleter;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xipki.common.qa.ValidationIssue;
import org.xipki.common.qa.ValidationResult;
import org.xipki.ocsp.client.api.OcspRequestor;
import org.xipki.ocsp.client.api.RequestOptions;
import org.xipki.ocsp.client.shell.CommonOcspStatusAction;
import org.xipki.ocsp.qa.Occurrence;
import org.xipki.ocsp.qa.OcspCertStatus;
import org.xipki.ocsp.qa.OcspQa;
import org.xipki.ocsp.qa.OcspResponseOption;
import org.xipki.ocsp.qa.shell.completer.OccurrenceCompleter;
import org.xipki.security.CrlReason;
import org.xipki.security.HashAlgo;
import org.xipki.security.IssuerHash;
import org.xipki.security.SecurityFactory;
import org.xipki.security.util.AlgorithmUtil;
import org.xipki.security.util.X509Util;
import org.xipki.shell.completer.DirCompleter;
import org.xipki.shell.completer.HashAlgCompleter;
import org.xipki.shell.completer.SigAlgCompleter;
import org.xipki.util.DateUtil;
import org.xipki.util.IoUtil;
import org.xipki.util.LogUtil;
import org.xipki.util.ReqRespDebug;
import org.xipki.util.ReqRespDebug.ReqRespPair;
import org.xipki.util.StringUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Command(scope = "xiqa", name = "batch-ocsp-status",
    description = "batch request status of certificates (QA)")
@Service
public class BatchOcspQaStatusAction extends CommonOcspStatusAction {

  private static final Logger LOG = LoggerFactory.getLogger(BatchOcspQaStatusAction.class);

  private static final String FILE_SEP = File.separator;

  @Option(name = "--resp-issuer",
      description = "certificate file of the responder's issuer")
  @Completion(FileCompleter.class)
  private String respIssuerFile;

  @Option(name = "--url", required = true, description = "OCSP responder URL")
  private String serverUrlStr;

  @Option(name = "--sn-file", required = true,
      description = "file containing the serial number and revocation information"
          + "\nEach line starts with # for comment or is of following format"
          + "\nserial-number[,status[,revocation-time]]")
  @Completion(FileCompleter.class)
  private String snFile;

  @Option(name = "--hex", description = "serial number without prefix is hex number")
  private Boolean hex = Boolean.FALSE;

  @Option(name = "--out-dir", required = true,
      description = "folder to save the request and response")
  @Completion(DirCompleter.class)
  private String outDirStr;

  @Option(name = "--save-req", description = "where to save the request")
  private Boolean saveReq = Boolean.FALSE;

  @Option(name = "--save-resp", description = "where to save the request")
  private Boolean saveResp = Boolean.FALSE;

  @Option(name = "--unknown-as-good",
      description = "where to expect the status good for unknown certificate")
  private Boolean unknownAsGood = Boolean.FALSE;

  @Option(name = "--no-sig-verify", description = "where to verify the signature")
  private Boolean noSigVerify = Boolean.FALSE;

  @Option(name = "--exp-sig-alg", description = "expected signature algorithm")
  @Completion(SigAlgCompleter.class)
  private String sigAlg;

  @Option(name = "--exp-nextupdate", description = "occurrence of nextUpdate")
  @Completion(OccurrenceCompleter.class)
  private String nextUpdateOccurrenceText = Occurrence.optional.name();

  @Option(name = "--exp-certhash",
      description = "occurrence of certHash, "
          + "will be set to forbidden for status unknown and issuerUnknown")
  @Completion(OccurrenceCompleter.class)
  private String certhashOccurrenceText = Occurrence.optional.name();

  @Option(name = "--exp-certhash-alg", description = "occurrence of certHash")
  @Completion(HashAlgCompleter.class)
  private String certhashAlg;

  @Option(name = "--exp-nonce", description = "occurrence of nonce")
  @Completion(OccurrenceCompleter.class)
  private String nonceOccurrenceText = Occurrence.optional.name();

  @Reference
  private SecurityFactory securityFactory;

  @Reference
  private OcspRequestor requestor;

  private Occurrence expectedCerthashOccurrence;

  private Occurrence expectedNextUpdateOccurrence;

  private Occurrence expectedNonceOccurrence;

  @Override
  protected final Object execute0() throws Exception {
    expectedCerthashOccurrence = Occurrence.forName(certhashOccurrenceText);
    expectedNextUpdateOccurrence = Occurrence.forName(nextUpdateOccurrenceText);
    expectedNonceOccurrence = Occurrence.forName(nonceOccurrenceText);

    File outDir = new File(outDirStr);
    File messageDir = new File(outDir, "messages");
    messageDir.mkdirs();

    File detailsDir = new File(outDir, "details");
    detailsDir.mkdirs();

    println("The result is saved in the folder " + outDir.getPath());

    String linuxIssuer = (respIssuerFile != null)
        ? "-CAfile ../../responder_issuer.pem" : "-no_cert_verify";

    String winIssuer = (respIssuerFile != null)
        ? "-CAfile ..\\..\\responder_issuer.pem" : "-no_cert_verify";

    String linuxMsg = "openssl ocsp -text ";

    String winMsg = "openssl ocsp -text ";

    String shellFilePath = null;

    if (saveReq && saveResp) {
      linuxMsg += linuxIssuer + " -reqin request.der -respin response.der";
      winMsg += winIssuer + " -reqin request.der -respin response.der";
      shellFilePath = new File(outDir, "verify-req-resp").getPath();
    } else if (saveReq) {
      linuxMsg += "-reqin request.der\n";
      winMsg += "-reqin request.der\n";
      shellFilePath = new File(outDir, "verify-req").getPath();
    } else if (saveResp) {
      linuxMsg += linuxIssuer + " -respin response.der\n";
      winMsg += winIssuer + " -respin response.der\n";
      shellFilePath = new File(outDir, "verify-resp").getPath();
    }

    if (shellFilePath != null) {
      File linuxShellFile = new File(shellFilePath + ".sh");
      IoUtil.save(linuxShellFile, ("#!/bin/sh\n" + linuxMsg).getBytes());
      IoUtil.save(shellFilePath + ".bat", ("@echo off\r\n" + winMsg).getBytes());
      linuxShellFile.setExecutable(true);
    }

    X509Certificate issuerCert = X509Util.parseCert(new File(issuerCertFile));

    X509Certificate respIssuer = null;
    if (respIssuerFile != null) {
      respIssuer = X509Util.parseCert(new File(respIssuerFile));
      IoUtil.save(new File(outDir, "responder-issuer.pem"),
          X509Util.toPemCert(respIssuer).getBytes());
    }

    RequestOptions requestOptions = getRequestOptions();

    IssuerHash issuerHash = new IssuerHash(
        HashAlgo.getNonNullInstance(requestOptions.getHashAlgorithmId()),
        Certificate.getInstance(issuerCert.getEncoded()));

    OutputStream resultOut = new FileOutputStream(new File(outDir, "overview.txt"));
    BufferedReader snReader = new BufferedReader(new FileReader(snFile));

    int numSucc = 0;
    int numFail = 0;

    try {
      URL serverUrl = new URL(serverUrlStr);

      OcspQa ocspQa = new OcspQa(securityFactory);

      // Content of a line:
      // <hex-encoded serial number>[,<reason code>,<revocation time in epoch seconds>]
      int lineNo = 0;
      String line;

      int sum = 0;
      long startDate = System.currentTimeMillis();
      long lastPrintDate = 0;
      while ((line = snReader.readLine()) != null) {
        lineNo++;
        line = line.trim();

        if (line.startsWith("#") || line.isEmpty()) {
          resultOut.write(line.getBytes());
          resultOut.write('\n');
          continue;
        }

        sum++;
        String resultText = lineNo + ": " + line + ": ";
        try {
          ValidationResult result = processOcspQuery(ocspQa, line, messageDir, detailsDir,
              serverUrl, respIssuer, issuerCert, issuerHash, requestOptions);
          if (result.isAllSuccessful()) {
            numSucc++;
            resultText += "valid";
          } else {
            numFail++;
            resultText += "invalid";
          }
        } catch (Throwable th) {
          LogUtil.error(LOG, th);
          numFail++;
          resultText += "error - " + th.getMessage();
        }

        println(resultText, resultOut);

        long now = System.currentTimeMillis();
        if (now - lastPrintDate > 980) { // use 980 ms to ensure the output every second.
          String duration = StringUtil.formatTime((now - startDate) / 1000, false);
          print("\rProcessed " + sum + " requests in " + duration);
          lastPrintDate = now;
        }
      }

      // unknown serial number
      lineNo++;
      SecureRandom random = new SecureRandom();
      byte[] bytes = new byte[16];
      random.nextBytes(bytes);
      bytes[0] = (byte) (0x7F & bytes[0]);
      BigInteger serialNumber = new BigInteger(bytes);

      String resultText = lineNo + ": " + serialNumber.toString(16) + ",unknown: ";
      try {
        ValidationResult result = processOcspQuery(ocspQa, serialNumber, OcspCertStatus.unknown,
            null, messageDir, detailsDir, serverUrl, respIssuer, issuerCert, issuerHash,
            requestOptions);
        if (result.isAllSuccessful()) {
          numSucc++;
          resultText += "valid";
        } else {
          numFail++;
          resultText += "invalid";
        }
      } catch (Throwable th) {
        LogUtil.error(LOG, th);
        numFail++;
        resultText += "error - " + th.getMessage();
      }

      sum++;
      print("\rProcessed " + sum + " requests in "
          + StringUtil.formatTime((System.currentTimeMillis() - startDate) / 1000, false));
      println("");

      println(resultText, resultOut);

      String message = StringUtil.concatObjectsCap(200, "=====BEGIN SUMMARY=====",
          "\n       url: ", serverUrlStr, "\n       sum: ", numFail + numSucc,
          "\nsuccessful: ", numSucc,      "\n    failed: ", numFail, "\n=====END SUMMARY=====");
      println(message);
      println(message, resultOut);
    } finally {
      snReader.close();
      resultOut.close();
    }

    return null;
  } // method execute0

  private ValidationResult processOcspQuery(OcspQa ocspQa, String line, File messageDir,
      File detailsDir, URL serverUrl, X509Certificate respIssuer, X509Certificate issuerCert,
      IssuerHash issuerHash, RequestOptions requestOptions) throws Exception {
    StringTokenizer tokens = new StringTokenizer(line, ",;:");

    int count = tokens.countTokens();
    BigInteger serialNumber;
    OcspCertStatus status = null;
    Date revTime = null;
    try {
      serialNumber = toBigInt(tokens.nextToken(), hex);

      if (count > 1) {
        String token = tokens.nextToken();
        if ("unknown".equalsIgnoreCase(token)) {
          status = OcspCertStatus.unknown;
        } else if ("good".equalsIgnoreCase(token)) {
          status = OcspCertStatus.good;
        } else {
          CrlReason reason = CrlReason.forNameOrText(token);
          switch (reason) {
            case AA_COMPROMISE:
              status = OcspCertStatus.aACompromise;
              break;
            case CA_COMPROMISE:
              status = OcspCertStatus.cACompromise;
              break;
            case AFFILIATION_CHANGED:
              status = OcspCertStatus.affiliationChanged;
              break;
            case CERTIFICATE_HOLD:
              status = OcspCertStatus.certificateHold;
              break;
            case CESSATION_OF_OPERATION:
              status = OcspCertStatus.cessationOfOperation;
              break;
            case KEY_COMPROMISE:
              status = OcspCertStatus.keyCompromise;
              break;
            case PRIVILEGE_WITHDRAWN:
              status = OcspCertStatus.privilegeWithdrawn;
              break;
            case SUPERSEDED:
              status = OcspCertStatus.superseded;
              break;
            case UNSPECIFIED:
              status = OcspCertStatus.unspecified;
              break;
            default:
              throw new Exception("invalid reason");
          }
        }
      } else {
        status = OcspCertStatus.good;
      }

      if (count > 2 && status != OcspCertStatus.good && status != OcspCertStatus.unknown) {
        revTime = DateUtil.parseUtcTimeyyyyMMddhhmmss(tokens.nextToken());
      }
    } catch (Exception ex) {
      LogUtil.warn(LOG, ex, "Could not parse line '" + line + "'");
      throw new IllegalArgumentException("illegal line");
    }

    return processOcspQuery(ocspQa, serialNumber, status, revTime, messageDir, detailsDir,
        serverUrl, respIssuer, issuerCert, issuerHash, requestOptions);
  }

  private ValidationResult processOcspQuery(OcspQa ocspQa, BigInteger serialNumber,
      OcspCertStatus status, Date revTime, File messageDir, File detailsDir,
      URL serverUrl, X509Certificate respIssuer, X509Certificate issuerCert,
      IssuerHash issuerHash, RequestOptions requestOptions) throws Exception {
    if (unknownAsGood && status == OcspCertStatus.unknown) {
      status = OcspCertStatus.good;
    }

    ReqRespDebug debug = null;
    if (saveReq || saveResp) {
      debug = new ReqRespDebug(saveReq, saveResp);
    }

    OCSPResp response;
    try {
      response = requestor.ask(issuerCert, serialNumber, serverUrl, requestOptions, debug);
    } finally {
      if (debug != null && debug.size() > 0) {
        ReqRespPair reqResp = debug.get(0);
        String filename = serialNumber.toString(16);

        if (saveReq) {
          byte[] bytes = reqResp.getRequest();
          if (bytes != null) {
            IoUtil.save(new File(messageDir, filename + FILE_SEP + "request.der"), bytes);
          }
        }

        if (saveResp) {
          byte[] bytes = reqResp.getResponse();
          if (bytes != null) {
            IoUtil.save(new File(messageDir, filename + FILE_SEP + "response.der"), bytes);
          }
        }
      } // end if
    } // end finally

    // analyze the result
    OcspResponseOption responseOption = new OcspResponseOption();
    responseOption.setNextUpdateOccurrence(expectedNextUpdateOccurrence);
    responseOption.setCerthashOccurrence(expectedCerthashOccurrence);
    responseOption.setNonceOccurrence(expectedNonceOccurrence);
    responseOption.setRespIssuer(respIssuer);
    responseOption.setSignatureAlgName(sigAlg);
    if (isNotBlank(certhashAlg)) {
      responseOption.setCerthashAlgId(AlgorithmUtil.getHashAlg(certhashAlg));
    }

    ValidationResult ret = ocspQa.checkOcsp(response, issuerHash, serialNumber, null, null,
        status, responseOption, revTime, noSigVerify.booleanValue());

    String validity = ret.isAllSuccessful() ? "valid" : "invalid";
    String hexSerial = serialNumber.toString(16);
    StringBuilder sb = new StringBuilder(50);
    sb.append("OCSP response for ").append(serialNumber).append(" (0x").append(hexSerial)
      .append(") is ").append(validity);

    for (ValidationIssue issue : ret.getValidationIssues()) {
      sb.append("\n");
      OcspQaStatusAction.format(issue, "    ", sb);
    }

    IoUtil.save(new File(detailsDir, hexSerial + "." + validity), sb.toString().getBytes());
    return ret;
  }

  private void println(String message, OutputStream out) throws IOException {
    out.write(message.getBytes());
    out.write('\n');
  }

}
