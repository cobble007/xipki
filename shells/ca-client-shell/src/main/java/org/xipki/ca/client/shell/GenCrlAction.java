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

import java.security.cert.X509CRL;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.xipki.ca.client.api.CaClientException;
import org.xipki.ca.client.api.PkiErrorException;
import org.xipki.util.ReqRespDebug;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Command(scope = "xi", name = "cmp-gencrl", description = "generate CRL")
@Service
public class GenCrlAction extends CrlAction {

  @Override
  protected X509CRL retrieveCrl() throws CaClientException, PkiErrorException {
    ReqRespDebug debug = getReqRespDebug();
    try {
      return caClient.generateCrl(caName, debug);
    } finally {
      saveRequestResponse(debug);
    }
  }

}
