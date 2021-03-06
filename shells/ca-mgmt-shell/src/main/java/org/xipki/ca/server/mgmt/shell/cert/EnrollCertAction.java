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

package org.xipki.ca.server.mgmt.shell.cert;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.FileCompleter;
import org.xipki.ca.server.mgmt.api.CaEntry;
import org.xipki.ca.server.mgmt.shell.CaAction;
import org.xipki.ca.server.mgmt.shell.completer.CaNameCompleter;
import org.xipki.ca.server.mgmt.shell.completer.ProfileNameCompleter;
import org.xipki.shell.CmdFailure;
import org.xipki.shell.completer.DerPemCompleter;
import org.xipki.util.DateUtil;
import org.xipki.util.IoUtil;
import org.xipki.util.StringUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Command(scope = "ca", name = "enroll-cert", description = "enroll certificate")
@Service
public class EnrollCertAction extends CaAction {

  @Option(name = "--ca", required = true, description = "CA name")
  @Completion(CaNameCompleter.class)
  private String caName;

  @Option(name = "--csr", required = true, description = "CSR file")
  @Completion(FileCompleter.class)
  private String csrFile;

  @Option(name = "--outform", description = "output format of the certificate")
  @Completion(DerPemCompleter.class)
  protected String outform = "der";

  @Option(name = "--out", aliases = "-o", required = true,
      description = "where to save the certificate")
  @Completion(FileCompleter.class)
  private String outFile;

  @Option(name = "--profile", aliases = "-p", required = true, description = "profile name")
  @Completion(ProfileNameCompleter.class)
  private String profileName;

  @Option(name = "--not-before", description = "notBefore, UTC time of format yyyyMMddHHmmss")
  private String notBeforeS;

  @Option(name = "--not-after", description = "notAfter, UTC time of format yyyyMMddHHmmss")
  private String notAfterS;

  @Override
  protected Object execute0() throws Exception {
    CaEntry ca = caManager.getCa(caName);
    if (ca == null) {
      throw new CmdFailure("CA " + caName + " not available");
    }

    Date notBefore = StringUtil.isNotBlank(notBeforeS)
        ? DateUtil.parseUtcTimeyyyyMMddhhmmss(notBeforeS) : null;

    Date notAfter = StringUtil.isNotBlank(notAfterS)
        ? DateUtil.parseUtcTimeyyyyMMddhhmmss(notAfterS) : null;

    byte[] encodedCsr = IoUtil.read(csrFile);

    X509Certificate cert = caManager.generateCertificate(caName, profileName, encodedCsr,
        notBefore, notAfter);
    saveVerbose("saved certificate to file", new File(outFile),
        derPemEncodeCert(cert.getEncoded(), outform));

    return null;
  }

}
