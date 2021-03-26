package dao;

public interface IGenericDao<T> {


	int create(final T entity);

	void createTableIfNotExist();
}
