//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Created on 22/10/2025, 19:15:57
 *
 * @author manso - computer
 */
public class FolderUtils {

    /**
     * CHATGPT version
     * <br>Deletes ALL contents of a folder (files and subfolders), while
     * optionally keeping or recreating the folder empty.
     *
     * @param folderPath Path of the folder to clean
     * @param recreate If true, the folder will be recreated or left empty after
     * deletion
     * @throws IOException If an error occurs while accessing the file system
     */
    public static void cleanFolder(String folderPath, boolean recreate) throws IOException {
        Path folder = Paths.get(folderPath);

        if (Files.notExists(folder)) {
            System.out.println("The folder does not exist: " + folder);
            if (recreate) {
                Files.createDirectories(folder);
                System.out.println("Folder created: " + folder);
            }
            return;
        }

        // Walk through all files and subdirectories, delete from bottom to top
        try (Stream<Path> paths = Files.walk(folder)) {
            paths
                    .sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(folder)) // do not delete the root folder itself
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Deleted: " + path);
                        } catch (IOException e) {
                            System.err.println("Error deleting " + path + ": " + e.getMessage());
                        }
                    });
        }
        // Recreate or ensure folder is empty
        if (recreate) {
            if (Files.notExists(folder)) {
                Files.createDirectories(folder);
            }
            System.out.println("Folder is now empty: " + folder);
        }
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510221915L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
