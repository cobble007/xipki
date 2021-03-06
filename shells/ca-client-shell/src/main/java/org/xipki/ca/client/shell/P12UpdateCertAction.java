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

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.FileCompleter;
import org.xipki.security.ConcurrentContentSigner;
import org.xipki.security.HashAlgo;
import org.xipki.security.SignerConf;
import org.xipki.util.ConfPairs;
import org.xipki.util.ObjectCreationException;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Command(scope = "xi", name = "cmp-update-p12",
    description = "update certificate (PKCS#12 keystore)")
@Service
public class P12UpdateCertAction extends UpdateCertAction {

  @Option(name = "--p12", required = true, description = "PKCS#12 keystore file")
  @Completion(FileCompleter.class)
  private String p12File;

  @Option(name = "--password", description = "password of the PKCS#12 keystore file")
  private String password;

  private ConcurrentContentSigner signer;

  @Override
  protected ConcurrentContentSigner getSigner() throws ObjectCreationException {
    if (signer == null) {
      if (password == null) {
        try {
          password = new String(readPassword());
        } catch (IOException ex) {
          throw new ObjectCreationException("could not read password: " + ex.getMessage(), ex);
        }
      }

      ConfPairs conf = new ConfPairs("password", password);
      conf.putPair("parallelism", Integer.toString(1));
      conf.putPair("keystore", "file:" + p12File);
      SignerConf signerConf = new SignerConf(conf.getEncoded(),
          HashAlgo.getNonNullInstance(hashAlgo), getSignatureAlgoControl());
      signer = securityFactory.createSigner("PKCS12", signerConf, (X509Certificate[]) null);
    }
    return signer;
  }

}
