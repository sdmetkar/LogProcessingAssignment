package dao;

public interface IGenericDao<T> {


	void create(final T entity);

	void createTableIfNotExist();
}
