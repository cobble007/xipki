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

package org.xipki.ca.server.api;

import java.security.cert.X509Certificate;
import java.util.Map;

import org.bouncycastle.asn1.cmp.PKIMessage;
import org.xipki.audit.AuditEvent;
import org.xipki.util.HealthCheckResult;

/**
 * TODO.
 * @author Lijun Liao
 * @since 3.0.1
 */

public interface CmpResponder {

  boolean isOnService();

  HealthCheckResult healthCheck();

  PKIMessage processPkiMessage(PKIMessage pkiMessage, X509Certificate tlsClientCert,
      Map<String, String> parameters, AuditEvent event);

  String getCaName();

}
