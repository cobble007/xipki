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

package org.xipki.ca.dbtool.port.ca;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xipki.ca.dbtool.port.DbPortWorker;
import org.xipki.ca.dbtool.port.DbPorter;
import org.xipki.datasource.DataSourceFactory;
import org.xipki.datasource.DataSourceWrapper;
import org.xipki.password.PasswordResolver;
import org.xipki.password.PasswordResolverException;
import org.xipki.util.IoUtil;
import org.xipki.util.ParamUtil;
import org.xipki.util.StringUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class CaDbExportWorker extends DbPortWorker {

  private static final Logger LOG = LoggerFactory.getLogger(CaDbExportWorker.class);

  private final DataSourceWrapper datasource;

  private final String destFolder;

  private final boolean resume;

  private final int numCertsInBundle;

  private final int numCertsPerSelect;

  public CaDbExportWorker(DataSourceFactory datasourceFactory, PasswordResolver passwordResolver,
      String dbConfFile, String destFolder, boolean resume, int numCertsInBundle,
      int numCertsPerSelect) throws PasswordResolverException, IOException {
    ParamUtil.requireNonBlank("dbConfFile", dbConfFile);
    ParamUtil.requireNonBlank("destFolder", destFolder);
    ParamUtil.requireNonNull("datasourceFactory", datasourceFactory);

    Properties props = DbPorter.getDbConfProperties(
        new FileInputStream(IoUtil.expandFilepath(dbConfFile)));
    this.datasource = datasourceFactory.createDataSource("ds-" + dbConfFile, props,
        passwordResolver);
    this.destFolder = IoUtil.expandFilepath(destFolder);
    this.resume = resume;
    this.numCertsInBundle = numCertsInBundle;
    this.numCertsPerSelect = numCertsPerSelect;
    checkDestFolder();
  }

  private void checkDestFolder() throws IOException {
    File file = new File(destFolder);
    if (!file.exists()) {
      file.mkdirs();
    } else {
      if (!file.isDirectory()) {
        throw new IOException(destFolder + " is not a folder");
      }

      if (!file.canWrite()) {
        throw new IOException(destFolder + " is not writable");
      }
    }

    File processLogFile = new File(destFolder, DbPorter.EXPORT_PROCESS_LOG_FILENAME);
    if (resume) {
      if (!processLogFile.exists()) {
        throw new IOException("could not process with '--resume' option");
      }
    } else {
      String[] children = file.list();
      if (children != null && children.length > 0) {
        throw new IOException(destFolder + " is not empty");
      }
    }
  } // method checkDestFolder

  @Override
  protected void run0() throws Exception {
    long start = System.currentTimeMillis();
    try {
      if (!resume) {
        // CAConfiguration
        CaconfDbExporter caConfExporter = new CaconfDbExporter(datasource, destFolder, stopMe);
        caConfExporter.export();
        caConfExporter.shutdown();
      }

      // CertStore
      CaCertstoreDbExporter certStoreExporter = new CaCertstoreDbExporter(datasource, destFolder,
          numCertsInBundle, numCertsPerSelect, resume, stopMe);
      certStoreExporter.export();
      certStoreExporter.shutdown();
    } finally {
      try {
        datasource.close();
      } catch (Throwable th) {
        LOG.error("datasource.close()", th);
      }
      long end = System.currentTimeMillis();
      System.out.println("Finished in " + StringUtil.formatTime((end - start) / 1000, false));
    }
  } // method run0

}
