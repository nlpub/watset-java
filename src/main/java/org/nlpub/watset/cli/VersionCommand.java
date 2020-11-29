/*
 * Copyright 2020 Dmitry Ustalov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.nlpub.watset.cli;

import com.beust.jcommander.Parameters;

import java.util.Optional;

/**
 * A command that reports the Watset version.
 */
@Parameters(commandDescription = "Version Information")
class VersionCommand extends Command {
    /**
     * Create an instance of command.
     *
     * @param parameters the parameters
     */
    public VersionCommand(MainParameters parameters) {
        super(parameters);
    }

    @Override
    public void run() {
        System.out.println("Watset " + getVersion().orElse("SNAPSHOT"));
        System.out.println("https://github.com/nlpub/watset-java");
        System.out.println();
        System.out.println("* Ustalov, D., Panchenko, A., Biemann, C., Ponzetto, S.P.:");
        System.out.println("  Watset: Local-Global Graph Clustering with Applications in Sense and Frame Induction.");
        System.out.println("  Computational Linguistics 45(3), 423-479 (2019).");
        System.out.println("  https://doi.org/10.1162/COLI_a_00354");
    }

    /**
     * Get the Watset package version, if possible.
     *
     * @return the version.
     */
    public Optional<String> getVersion() {
        return Optional.ofNullable(getClass().getPackage().getImplementationVersion());
    }
}
