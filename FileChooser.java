import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.awt.*;
import java.util.*;
import javax.swing.filechooser.*;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class FileChooser extends JPanel {
   JFileChooser chooser;
   FileFilter filter;
   String filename;
   String fileExtension;
  
   public FileChooser() {
    ArrayList<String> filterList = new ArrayList<String>();
    for (String element : ImageIO.getReaderFileSuffixes()) {
        filterList.add(element);
    }

    filterList.add("txt");

    this.filter = new FileNameExtensionFilter(
        "Image & Text files", filterList.toArray(new String[0]));   

    this.chooser = new JFileChooser(); 
    
   }

    public File openFileChooser() {
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Choose a File");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(filter);

        //
        // disable the "All files" option.
        //
        chooser.setAcceptAllFileFilterUsed(false);  
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
            filename = chooser.getSelectedFile().getName();
            fileExtension = filename.substring(filename.lastIndexOf("."), filename.length());
            return chooser.getSelectedFile();
            }

        else {
            return null;
        }

    }

    public File openDirectoryChooser() {
        File directory;

        // chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Choose a Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
        //
        // disable the "All files" option.
        //
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { 
            directory = chooser.getSelectedFile();
            return directory;
            }
    
        else {
            return null;
        }
    
    }
   
  public Dimension getPreferredSize(){
    return new Dimension(200, 200);
    }
    
//   public static void main(String s[]) {
//     JFrame frame = new JFrame("");
//     FileChooser panel = new FileChooser();
//     frame.addWindowListener(
//       new WindowAdapter() {
//         public void windowClosing(WindowEvent e) {
//           System.exit(0);
//           }
//         }
//       );
//     frame.getContentPane().add(panel,"Center");
//     frame.setSize(panel.getPreferredSize());
//     frame.setVisible(true);
//     }
}