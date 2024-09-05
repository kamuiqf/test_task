import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        String id = document.getId();
        if (id == null) {
            id = UUID.randomUUID().toString();
            document.setId(id);
            document.setCreated(Instant.now());
        } else {
            Document existingDocument = storage.get(id);
            if (existingDocument != null) {
                document.setCreated(existingDocument.getCreated());
            }
        }
        return storage.put(id, document);
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        List<Document> documents = new ArrayList<>();
        for (Document document : storage.values()) {
            if (match(document, request)) {
                documents.add(document);
            }
        }
        return documents;
    }

    private boolean match(Document document, SearchRequest request) {
        boolean result = false;

        List<String> titlePrefixes = request.titlePrefixes;
        if (titlePrefixes != null) {
            result = titlePrefixes.stream()
                        .anyMatch(titlePrefix -> Objects.equals(titlePrefix, document.getTitle()));
        }

        List<String> containsContents = request.containsContents;
        if (containsContents != null) {
            result = containsContents.stream()
                        .anyMatch(content -> Objects.equals(content, document.getContent()));
        }

        List<String> authorIds = request.authorIds;
        if (authorIds != null) {
            result = authorIds.stream()
                        .anyMatch(authorId -> Objects.equals(authorId, document.getAuthor().getId()));
        }

        if (request.createdFrom != null && request.createdTo != null) {
            Instant created = document.created;
            result = created.isAfter(request.createdFrom) && created.isBefore(request.createdTo);
        }

        return result;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}