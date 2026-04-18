# SQL Index Recommendations for Event Search

This document provides SQL index recommendations to optimize event search queries.

## Recommended Indexes

### 1. Composite Index for Status, Visibility, and Date Filtering
```sql
CREATE INDEX idx_events_status_public_startdate
ON events (status, is_public, start_date);
```
**Usage**: Optimizes queries filtering by status (e.g., PUBLISHED) and public visibility, ordered by start date.

### 2. Category Foreign Key Index
```sql
CREATE INDEX idx_events_category_id
ON events (category_id);
```
**Usage**: Optimizes joins and filters by category.

### 3. Country Foreign Key Index
```sql
CREATE INDEX idx_events_country_id
ON events (country_id);
```
**Usage**: Optimizes joins and filters by country.

### 4. Organizer Foreign Key Index
```sql
CREATE INDEX idx_events_organizer_id
ON events (organizer_id);
```
**Usage**: Optimizes queries filtering by event organizer.

### 5. Composite Index for Ticketing
```sql
CREATE INDEX idx_events_seats_tickets
ON events (has_seats, available_tickets)
WHERE available_tickets > 0;
```
**Usage**: Optimizes queries for events with available tickets. The partial index condition reduces index size.

### 6. Geographic Bounding Box Index
```sql
CREATE INDEX idx_events_location_coords
ON events (latitude, longitude)
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
```
**Usage**: Optimizes geographic searches within bounding boxes. Partial index only for events with coordinates.

### 7. Start Date Index for Upcoming Events
```sql
CREATE INDEX idx_events_start_date
ON events (start_date DESC);
```
**Usage**: Optimizes sorting and filtering by start date, especially for upcoming events.

### 8. Ticket Sales Active with Dates
```sql
CREATE INDEX idx_events_ticket_sales_active
ON events (is_ticket_sales_active, ticket_sales_start_date, ticket_sales_end_date);
```
**Usage**: Optimizes queries for events with active ticket sales.

### 9. End Date Index
```sql
CREATE INDEX idx_events_end_date
ON events (end_date);
```
**Usage**: Optimizes date range queries filtering by event end date.

### 10. Location Text Index
```sql
CREATE INDEX idx_events_location
ON events (LOWER(location));
```
**Usage**: Optimizes case-insensitive location searches.

## Performance Validation

Use `EXPLAIN ANALYZE` to validate index usage:

```sql
EXPLAIN ANALYZE
SELECT e.id, e.name, e.start_date, e.end_date, e.location,
       e.latitude, e.longitude, e.has_seats, e.available_tickets,
       c.id as category_id, c.name as category_name,
       co.id as country_id, co.name as country_name, co.code as country_code
FROM events e
LEFT JOIN categories c ON e.category_id = c.id
LEFT JOIN countries co ON e.country_id = co.id
WHERE e.status = 'PUBLISHED'
  AND e.is_public = true
  AND e.start_date >= CURRENT_TIMESTAMP
  AND e.available_tickets > 0
ORDER BY e.start_date ASC
LIMIT 20;
```

### Expected Output
- Should show "Index Scan" or "Bitmap Index Scan" instead of "Seq Scan"
- Execution time should be < 50ms for tables with < 100k rows
- Check that the composite indexes are being used

## Index Maintenance

### Monitor Index Usage
```sql
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
  AND tablename = 'events'
ORDER BY idx_scan DESC;
```

### Check Index Size
```sql
SELECT
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
  AND tablename = 'events';
```

### Rebuild Indexes (if needed)
```sql
REINDEX TABLE events;
```

## Notes

1. **Production Considerations**:
   - Consider using PostgreSQL's pg_trgm extension for fuzzy text search on event names/descriptions
   - For true geospatial queries, consider PostGIS extension
   - Monitor index bloat and rebuild periodically

2. **Index Trade-offs**:
   - Indexes improve SELECT performance but slow down INSERT/UPDATE/DELETE
   - Balance between query performance and write performance
   - Monitor index usage and remove unused indexes

3. **Future Enhancements**:
   - Full-text search index on name and description fields
   - Consider Elasticsearch for advanced search features
   - Implement materialized views for complex aggregations
