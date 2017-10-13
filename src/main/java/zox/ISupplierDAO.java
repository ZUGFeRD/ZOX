package zox;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface ISupplierDAO {
		  @SqlUpdate("create table something (id int primary key NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name varchar(100))")
		  void createSomethingTable();

		  @SqlUpdate("insert into something (name) values (:name)")
		  void insert(@Bind("name") String name);

		  @SqlQuery("select name from something where id = :id")
		  String findNameById(@Bind("id") int id);

}
