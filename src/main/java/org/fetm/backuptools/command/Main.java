package org.fetm.backuptools.command;

import org.fetm.backuptools.common.BackupAgent;
import org.fetm.backuptools.common.datanode.*;
import org.fetm.backuptools.common.tools.ScpClient;
import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/******************************************************************************
 * Copyright (c) 2013,2014. Florian Mahon <florian@faivre-et-mahon.ch>        *
 *                                                                            *
 * This file is part of backuptools.                                          *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * any later version.                                                         *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * General Public License for more details. You should have received a        *
 * copy of the GNU General Public License along with this program.            *
 * If not, see <http://www.gnu.org/licenses/>.                                *
 ******************************************************************************/

public class Main {
    public void main(String[] args) throws IOException {
        Options options = new Options();

        options.addOption("h"    , false,"Display this help");
        options.addOption("f"    , true ,"set configuration file");
        options.addOption("i"    , true ,"initialise new file configuration");
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            usage(options);
            System.exit(-1);
        }

        if(cmd.hasOption("i")){
            String filename = cmd.getOptionValue("i");
            Path file = Paths.get(filename);
            initConfigFile(file);
        }
        else if(cmd.hasOption("h")){
            usage(options);
        } else if(cmd.hasOption("f")){
            String file = cmd.getOptionValue("f");
            BackupAgent agent = getBackupAgent(file);
            agent.doBackup();
        } else if(cmd.hasOption("d")){
            String directory = cmd.getOptionValue("d");
        }
    }

    private void initConfigFile(Path file) throws IOException {
        URL URLFile = getClass().getClassLoader().getResource("configuration.properties");
        InputStream inputStream = URLFile.openStream();
        Files.copy(inputStream, file);
    }

    private static BackupAgent getBackupAgent(String file) throws IOException {
        Path path_file = Paths.get(file);
        Properties properties = new Properties();
        properties.load(new FileInputStream(path_file.toFile()));
        String src_directory  = properties.getProperty("src");
        String vault_location = properties.getProperty("vault.directory");
        String vault_type     = properties.getProperty("vault.type");
        IWORMFileSystem fs = null;
        if(vault_type.equals("sftp")){
            String ssh_user       = properties.getProperty("vault.ssh.user");
            String ssh_host       = properties.getProperty("vault.ssh.host");
            String ssh_pass       = properties.getProperty("vault.ssh.password");

            ScpClient scp = new ScpClient(ssh_host, ssh_user, ssh_pass);
            fs = new WORMSftpFileSystem(scp,vault_location);
        }else if (vault_type.equals(("dir"))){
            fs = new WORMFileSystem(vault_location);
        }

        INodeDatabase db = new NodeDirectoryDatabase(fs);
        return new BackupAgent(Paths.get(src_directory), db);
    }

    private static void usage(Options options) {
        HelpFormatter hp = new HelpFormatter();
        hp.printHelp("backuptools",options);
    }
}