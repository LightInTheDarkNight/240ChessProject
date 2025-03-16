package dataaccess;

public interface DAO<T extends Record, K> {
    /**
     * Removes all data from the attached database.
     *
     * @return true if successful, false otherwise.
     */
    boolean clear() throws DataAccessException;

    /**
     * Adds the given game to the attached database if an item with the given item's key does not already exist.
     *
     * @param item the item to add to the attached database.
     * @return true if item was added successfully; false otherwise.
     */
    boolean add(T item) throws DataAccessException;

    /**
     * Retrieves the item associated with the passed in key.
     *
     * @param key the ID of the item to retrieve.
     * @return the item retrieved from the database, or null if it doesn't exist.
     */
    T get(K key) throws DataAccessException;

    /**
     * Retrieves the item in the database associated with the passed in item's key value, if it exists.
     *
     * @param item the item with the ID of the item to retrieve.
     * @return the item retrieved from the database, or null if it doesn't exist.
     */
    T get(T item) throws DataAccessException;

    /**
     * Removes the item passed in from the database.
     *
     * @param item the item to remove from the database.
     * @return true if the operation succeeded; false otherwise.
     */
    boolean delete(T item) throws DataAccessException;

    /**
     * Removes the item with the given key from the database.
     *
     * @param key the key of the item to remove.
     * @return true successful; false otherwise.
     */
    boolean delete(K key) throws DataAccessException;


}
