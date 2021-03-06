package by.bsu.mmf.devteam.database.dao;

import by.bsu.mmf.devteam.database.connector.DBConnector;
import by.bsu.mmf.devteam.exception.data.DAOException;
import by.bsu.mmf.devteam.logic.bean.entity.Specification;
import by.bsu.mmf.devteam.resource.ResourceManager;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements DAO pattern
 *
 * @author Dmitry Petrovich
 * @since 1.0.0-alpha
 */
public class SpecificationDAO extends AbstractDAO {
    /** Initializing database activity logger */
    private static Logger logger = Logger.getLogger("db");

    /** Specification logger messages */
    private static final String ERROR_GET_SPEC_NAME = "logger.db.error.get.specification.name";
    private static final String INFO_GET_SPEC_NAME = "logger.db.info.get.specification.name";
    private static final String ERROR_GET_SPEC_STATUS = "logger.db.error.get.specification.status";
    private static final String INFO_GET_SPEC_STATUS = "logger.db.info.get.specification.status";
    private static final String ERROR_GET_USER_SPECS = "logger.db.error.get.user.specifications";
    private static final String INFO_GET_USER_SPECS = "logger.db.info.get.user.specifications";
    private static final String ERROR_GET_WAITING_SPECS = "logger.db.error.get.waiting.specifications";
    private static final String INFO_GET_WAITING_SPECS = "logger.db.info.get.waiting.specifications";
    private static final String ERROR_SAVE_SPEC = "logger.db.error.save.specification";
    private static final String INFO_SAVE_SPEC = "logger.db.info.save.specification";
    private static final String ERROR_GET_LAST_SPEC_ID = "logger.db.error.get.last.spec.id";
    private static final String INFO_GET_LAST_SPEC_ID = "logger.db.info.get.last.spec.id";
    private static final String ERROR_SET_SPEC_STATUS = "logger.db.error.set.spec.status";
    private static final String INFO_SET_SPEC_STATUS = "logger.db.info.set.spec.status";
    private static final String ERROR_GET_AUTHOR_OF_SPEC = "logger.db.error.get.author.id.of.spec";
    private static final String INFO_GET_AUTHOR_OF_SPEC = "logger.db.info.get.author.id.of.spec";

    /** Keeps default specification status [waiting] */
    private static final String DEFAULT_SPECIFICATION_STATUS = "waiting";

    /**
     * This query searches name of specification by specification id.
     */
    public static final String SQL_FIND_SPECIFICATION_NAME_BY_ID =
            "SELECT name FROM specifications WHERE id = ?";

    /**
     * This query searches specification status by specification id.
     */
    public static final String SQL_FIND_SPECIFICATION_STATUS_BY_ID =
            "SELECT status FROM specifications WHERE id = ?";

    /**
     * This query searches all customer specifications.
     */
    public static final String SQL_FIND_SPECIFICATIONS_BY_CUSTOMER_ID =
            "SELECT * FROM specifications WHERE uid=? ORDER BY id DESC";

    /**
     * This query searches all waiting specifications and customer id for each specification.
     */
    private static final String SQL_FIND_WAITING_SPECIFICATIONS =
            "SELECT specifications.id, specifications.uid, specifications.name, specifications.`status`, " +
            "users.id, users.mail FROM specifications " +
            "INNER JOIN users ON specifications.uid = users.id " +
            "WHERE status = ? ORDER BY specifications.id ASC";

    /**
     * This query update specification status.
     */
    public static final String SQL_SET_SPECIFICATION_STATUS_TO =
            "UPDATE specifications SET status = ? WHERE id = ?";

    /**
     *  This query saves new customer specification. <br />
     *  Requires to set: user id, specification name, specification status.
     */
    public static final String SQL_INSERT_NEW_SPECIFICATION =
            "INSERT INTO specifications (uid, name, status) VALUES (?, ?, ?)";

    /**
     * This query searches last created specification by customer.
     */
    public static final String SQL_FIND_LAST_CUSTOMER_SPECIFICATION_ID =
            "SELECT MAX(id) FROM specifications WHERE uid = ?";

    /**
     * This query searches user id who created certain specification.
     */
    public static final String SQL_FIND_USER_ID_BY_SPECIFICATION_ID =
            "SELECT uid FROM specifications WHERE id = ?";

    /**
     * Returns name of specification
     *
     * @param id Specification id
     * @return Specification name
     * @throws DAOException object if execution of query is failed
     */
    public String getSpecificationName(int id) throws DAOException {
        String name = "";
        connector = new DBConnector();
        try {
            preparedStatement = connector.getPreparedStatement(SQL_FIND_SPECIFICATION_NAME_BY_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                name = resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_GET_SPEC_NAME) + id, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_GET_SPEC_NAME) + id);
        return name;
    }

    /**
     * This method return specification status by id
     *
     * @param id Specification id
     * @return Specification status
     * @throws DAOException object if execution of query is failed
     */
    public String getSpecificationStatus(int id) throws DAOException {
        String status = "";
        connector = new DBConnector();
        try {
            preparedStatement = connector.getPreparedStatement(SQL_FIND_SPECIFICATION_STATUS_BY_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                status = resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_GET_SPEC_STATUS) + id, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_GET_SPEC_STATUS) + id);
        return status;
    }

    /**
     * Returns list of all customer specifications
     *
     * @param id Customer id
     * @return List of specifications
     * @throws DAOException object if execution of query is failed
     */
    public List<Specification> getUserSpecifications(int id) throws DAOException{
        connector = new DBConnector();
        List<Specification> list = new ArrayList<Specification>();
        try {
            PreparedStatement statement = connector.getPreparedStatement(SQL_FIND_SPECIFICATIONS_BY_CUSTOMER_ID);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Specification specification = new Specification();
                specification.setId(resultSet.getInt(1));
                specification.setName(resultSet.getString(3));
                specification.setStatus(resultSet.getString(4));
                JobDAO jobsDAO = new JobDAO();
                specification.setJobs(jobsDAO.getNumberOfJobsInSpecification(resultSet.getInt(1)));
                list.add(specification);
            }
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_GET_USER_SPECS) + id, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_GET_USER_SPECS) + id);
        return list;
    }

    /**
     * This method returns list of waiting specifications
     *
     * @return List of specifications
     * @throws DAOException object if execution of query is failed
     */
    public List<Specification> getWaitingSpecifications() throws DAOException{
        connector = new DBConnector();
        List<Specification> list = new ArrayList<>();
        try {
            preparedStatement = connector.getPreparedStatement(SQL_FIND_WAITING_SPECIFICATIONS);
            preparedStatement.setBytes(1, DEFAULT_SPECIFICATION_STATUS.getBytes());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Specification spec = new Specification();
                spec.setId(resultSet.getInt(1));
                spec.setEmail(resultSet.getString(6));
                spec.setName(resultSet.getString(3));
                JobDAO jobsDAO = new JobDAO();
                spec.setJobs(jobsDAO.getNumberOfJobsInSpecification(resultSet.getInt(1)));
                list.add(spec);
            }
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_GET_WAITING_SPECS), e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_GET_WAITING_SPECS));
        return list;
    }

    /**
     * This method saves new specification in database
     *
     * @param id User id
     * @param name Specification name
     * @return Id of saved specification
     * @throws DAOException object if execution of query is failed
     */
    public int saveSpecification(int id, String name) throws DAOException{
        connector = new DBConnector();
        try {
            preparedStatement = connector.getPreparedStatement(SQL_INSERT_NEW_SPECIFICATION);
            preparedStatement.setInt(1, id);
            preparedStatement.setBytes(2, (new String(name.getBytes("UTF-8"), "CP1251")).getBytes());
            preparedStatement.setBytes(3, DEFAULT_SPECIFICATION_STATUS.getBytes());
            preparedStatement.execute();
        } catch (SQLException | UnsupportedEncodingException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_SAVE_SPEC) + id, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_SAVE_SPEC) + id);
        return getLastSpecificationId(id);
    }

    /**
     * This method returns specification id which created by certain user
     *
     * @param userId User id
     * @return Specification id
     * @throws DAOException object if execution of query is failed
     */
    public int getLastSpecificationId(int userId) throws DAOException{
        connector = new DBConnector();
        int id = 0;
        try {
            preparedStatement = connector.getPreparedStatement(SQL_FIND_LAST_CUSTOMER_SPECIFICATION_ID);
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_GET_LAST_SPEC_ID) +userId, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_GET_LAST_SPEC_ID) + userId);
        return id;
    }

    /**
     * This method sets specification status
     *
     * @param id Specification id
     * @param status Status
     * @throws DAOException object if execution of query is failed
     */
    public void setSpecificationStatus(int id, String status) throws DAOException{
        connector = new DBConnector();
        try {
            preparedStatement = connector.getPreparedStatement(SQL_SET_SPECIFICATION_STATUS_TO);
            preparedStatement.setBytes(1, status.getBytes());
            preparedStatement.setInt(2, id);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_SET_SPEC_STATUS) + status, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_SET_SPEC_STATUS));
    }

    /**
     * This method returns customer id who created certain specification.
     *
     * @param sid Specification id
     * @return Customer id
     * @throws DAOException object if execution of query is failed
     */
    public int getUserId(int sid) throws DAOException {
        int id = 0;
        connector = new DBConnector();
        try {
            preparedStatement = connector.getPreparedStatement(SQL_FIND_USER_ID_BY_SPECIFICATION_ID);
            preparedStatement.setInt(1, sid);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException(ResourceManager.getProperty(ERROR_GET_AUTHOR_OF_SPEC) + sid, e);
        } finally {
            connector.close();
        }
        logger.info(ResourceManager.getProperty(INFO_GET_AUTHOR_OF_SPEC) + sid);
        return id;
    }

}
