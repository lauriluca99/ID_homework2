package lucenex;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
 

import java.io.*;



public class QueryReader{

    private static StandardAnalyzer analyzer = new StandardAnalyzer();

    public static void main(String[] args) throws IOException {
    
   //=========================================================
        // Now search
        //=========================================================
        String indexLocation = "tmp/index";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation).toPath()));
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector;

    
        BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in));
        String s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                //metto dentro collector sennò posso eseguire una query alla volta
                collector = TopScoreDocCollector.create(5, 1000);
                System.out.println("Enter the search query (q=quit):");
                System.out.println("nome: [nome del file]  --> per cercare il nome file");
                System.out.println("contenuto: [contenuto del file]  --> per cercare all'interno del file");

                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                Query q = new QueryParser("contenuto", analyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Trovati " + collector.getTotalHits() + " hits.");
                System.out.println("Riportati i primi " + hits.length + " risultati");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
                }

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : " + e.getMessage());
            }
        }

    }

    
}