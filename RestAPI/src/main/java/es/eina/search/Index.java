package es.eina.search;

import org.apache.commons.lang3.Validate;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

public abstract class Index {

    public static final String ID_INDEX_COLUMN = "id";
    public static final String NAME_INDEX_COLUMN = "name";
    public static final String DESC_INDEX_COLUMN = "desc";
    public static final String RATE_INDEX_COLUMN = "rate";
    protected static final StandardAnalyzer indexAnalyzer = new StandardAnalyzer(Version.LUCENE_40);
    protected static final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, indexAnalyzer);

    private static final Field.Store STORE = Field.Store.YES;


    static {
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    }

    private IndexWriter writer;
    private FSDirectory directory;
    private DirectoryReader reader;
    private Document current_document;

    /**
     * Creates an Index, opens that Index and reload the data this Index contains in a directory.
     * @param path : Path to the directory containing this index.
     */
    public final void openIndex(String path) {
        try {
            File dir = new File(path);
            System.out.println("dir: " + dir.getAbsolutePath());
            directory = FSDirectory.open(dir);
        } catch (IOException e) {
            System.out.println("Cannot create product index directory.");
            e.printStackTrace();
        }
        reloadIndex();

        try {
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            System.out.println("Cannot create product index directory.");
            e.printStackTrace();
        }
    }

    /**
     * Create a new Index Writer
     */
    protected void constructWriter() {
        try {
            writer = new IndexWriter(getDirectory(), config);
            writer.commit();
        } catch (IOException e) {
            System.out.println("Cannot create IndexWriter");
            e.printStackTrace();
        }
    }

    protected IndexWriter getWriter() {
        return writer;
    }

    protected FSDirectory getDirectory() {
        return directory;
    }

    protected DirectoryReader getReader() {
        return reader;
    }

    /**
     * Fetch a Document in the index by its id.
     * @param id : Id of Document to search
     * @return A Document identified by the provided id.
     */
    public final Document getDocument(int id) {
        try {
            return reader.document(id);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes an Index and remove all the data it contains.
     */
    protected final void closeIndexWriter() {
        if (writer != null) {
            System.out.println("Removing index data.");
            try {
                writer.deleteAll();
                writer.close();
                writer = null;
            } catch (IOException e) {
                System.out.println("Cannot close previous indexWriter");
                e.printStackTrace();
            }
        }
    }

    /**
     * Commits the data added to the Index to the Directory
     */
    protected void commitDataToIndex() {
        try {
            writer.commit();
            writer.close();
        } catch (IOException e) {
            System.out.println("Cannot close IndexWriter");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new Document to start adding fields
     */
    protected void createCurrentDocument() {
        this.current_document = new Document();
    }

    /**
     * Stores the current work document in the index.<br>
     * After calling this method, no calls to {@link Index#storeInDocument(String, Object)} is allowed unless
     * a new call to {@link Index#createCurrentDocument()} is made.<br>
     * @throws RuntimeException If there is no current document.
     */
    protected void addCurrentDocumentToIndex() {
        Validate.notNull(current_document, "Error adding current document to Index, create one first.");
        try {
            writer.addDocument(current_document);
        } catch (IOException e) {
            System.out.println("Cannot save data to index.");
            e.printStackTrace();
        }

        current_document = null;
    }

    /**
     * Add a new field to the current document as a pair (key, value).
     * @param key : Key of the field.
     * @param value : Value of the field.
     * @param <T> : String, Integer, Float or Double.
     * @throws RuntimeException If {@link T} is not a recognizable and valid type.
     */
    protected <T> void storeInDocument(String key, T value) {
        Field f;
        if (value instanceof String) {
            f = new TextField(key, (String) value, STORE);
        } else if (value instanceof Integer) {
            f = new IntField(key, (Integer) value, STORE);
        } else if (value instanceof Long) {
            f = new LongField(key, (Long) value, STORE);
        } else if (value instanceof Float) {
            f = new FloatField(key, (Float) value, STORE);
        } else if (value instanceof Double) {
            f = new DoubleField(key, (Double) value, STORE);
        } else if (value instanceof Date) {
            f = new LongField(key, ((Date) value).getTime(), STORE);
        } else {
            throw new RuntimeException("Cannot add an unknown type to index (" + value + ").");
        }
        current_document.add(f);
    }

    /**
     * Reloads the data of this index
     */
    protected abstract void reloadIndex();

    /**
     * Perform a search
     * @param query : Query keywords
     * @param searchAmount : Maximum amount of results
     * @return A list of all results queried matching the parameters.
     */
    public abstract List<ScoreDoc> search(String query, int searchAmount);


    /**
     * Prints all the documents in this index.<br>
     * <b>NOTE: </b> It is only used for debug purposes.
     */
    protected void printAllDocumentsInIndex() {
        try {
            @SuppressWarnings("deprecated")
            IndexReader reader = IndexReader.open(directory);
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                System.out.println("name: " + doc.get(NAME_INDEX_COLUMN));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        closeIndexWriter();
    }
}
