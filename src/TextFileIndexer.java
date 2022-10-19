package lucenex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class TextFileIndexer {

    Map<String,Analyzer> perFieldAnalyzers = new HashMap<>();
    CharArraySet stopWords = new CharArraySet(Arrays.asList("txt"),true);
    Analyzer analyzer ;
   
    //private static StandardAnalyzer analyzer = new StandardAnalyzer();

    private IndexWriter writer;
    private List<File> queue = new ArrayList<File>();


    public static void main(String[] args) throws IOException {
        System.out.println("index will be created in tmp/index folder");

        String indexLocation = "tmp/index";
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String s = "";

        TextFileIndexer indexer = null;
        try {
            indexer = new TextFileIndexer(indexLocation);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
                System.out.println("[.txt]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                long startTime = System.nanoTime();
                //try to add file into the index
                indexer.indexFileOrDirectory(s);
                long endTime = System.nanoTime();
                System.out.println("tempo di indicizzazione = " + (endTime-startTime)/1000000 + " millisecondi");
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : " + e.getMessage());
            }
        }

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        indexer.closeIndex();

        

    }

    /**
     * Constructor
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    TextFileIndexer(String indexDir) throws IOException {
        // the boolean true parameter means to create a new index everytime,
        // potentially overwriting any existing files there.


        // utilizzo due analyzer, uno per contenuto e uno per nome
        perFieldAnalyzers.put("nome", new StopAnalyzer(stopWords));
        perFieldAnalyzers.put("contenuto", new StandardAnalyzer());
        analyzer = new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), perFieldAnalyzers);
     

        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());


        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(dir, config);
        writer.deleteAll();
    }

        

    /**
     * Indexes a file or directory
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.getDocStats().numDocs;
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================
                fr = new FileReader(f);
                doc.add(new TextField("contenuto", fr));
                doc.add(new TextField("path", f.getPath(), Field.Store.YES));
                doc.add(new TextField("nome", f.getName(), Field.Store.YES));

                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.getDocStats().numDocs;
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            //===================================================
            // Only index text files
            //===================================================
            if (filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.commit();
        writer.close();
    }
}

