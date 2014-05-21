package org.fetm.backuptools.command;

import org.apache.commons.cli.*;
import org.fetm.backuptools.common.BackupAgentFactory;
import org.fetm.backuptools.common.IBackupAgent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*****************************************************************************
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
 * ***************************************************************************/

public class Main {

    public static final String OPT_HELP = "h";
    public static final String OPT_FILE = "f";
    public static final String OPT_INIT = "i";
    public static final String OPT_DIRECTORY = "d";

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.execute(args);
        System.exit(0);
    }

    private void execute(String[] args) throws IOException {

        Options options = new Options();
        List<IBackupAgent> agents = new ArrayList<>();
        options.addOption(OPT_HELP, false, "Display this help");
        options.addOption(OPT_FILE, true, "set configuration file");
        options.addOption(OPT_INIT, true, "create new configuration file");
        options.addOption(OPT_DIRECTORY, true,  "set directory contain multi configurations files '*.properties'");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.getOptions().length == 0) {
                throw new Exception("Require option!");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            usage(options);
            System.exit(-1);
        }

        if (cmd.hasOption(OPT_INIT)) {
            String filename = cmd.getOptionValue(OPT_INIT);
            Path file = Paths.get(filename);
            initConfigFile(file);
        } else if (cmd.hasOption(OPT_HELP)) {
            usage(options);
        } else if (cmd.hasOption(OPT_FILE)) {
            String file = cmd.getOptionValue(OPT_FILE);
            agents.add(BackupAgentFactory.getBackupAgent(file));
        } else if (cmd.hasOption(OPT_DIRECTORY)) {
            String directory = cmd.getOptionValue(OPT_DIRECTORY);
            Path path = Paths.get(directory);
            if(path.toFile().isDirectory()){
                agents.addAll(BackupAgentFactory.getBackupAgents(path));
            }
        }

        for(IBackupAgent agent : agents){
            agent.doBackup();
        }
    }



    private void initConfigFile(Path file) throws IOException {
        URL URLFile = getClass().getClassLoader().getResource("configuration.properties");
        InputStream inputStream = URLFile.openStream();
        Files.copy(inputStream, file);
    }

    private void usage(Options options) {
        HelpFormatter hp = new HelpFormatter();
        hp.printHelp("backuptools", options);
    }
}