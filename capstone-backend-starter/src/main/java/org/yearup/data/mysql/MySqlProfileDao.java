package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    private static final Logger logger = LoggerFactory.getLogger(MySqlProfileDao.class);

    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile)
    {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = getConnection())
        {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();
            logger.info("Profile created for user_id: {}", profile.getUserId());

            return profile;
        }
        catch (SQLException e)
        {
            logger.error("Error creating profile for user_id: {}", profile.getUserId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getByUserId(int userId) {
        String query = "SELECT * FROM profiles WHERE user_id = ?;";

        try (
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, userId);

            try (
                    ResultSet resultSet = preparedStatement.executeQuery();
                )
            {
                if(resultSet.next()) {
                    logger.info("Profile retrieved for user_id: {}", userId);
                    return  mapRow(resultSet);
                }
                else {
                    logger.warn("No profile found for user_id: {}", userId);
                }
            }

        } catch (SQLException e) {
            logger.error("Error retrieving profile for user_id: {}", userId, e);
//            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(Profile profile) {
        String query = "UPDATE profiles " +
                "SET first_name = ?, " +
                "last_name = ?, " +
                "phone = ?, " +
                "email = ?, " +
                "address = ?, " +
                "city = ?, " +
                "state = ?, " +
                "zip = ? " +
                "WHERE user_id = ?;";

        try (
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setString(1, profile.getFirstName());
            preparedStatement.setString(2, profile.getLastName());
            preparedStatement.setString(3, profile.getPhone());
            preparedStatement.setString(4, profile.getEmail());
            preparedStatement.setString(5, profile.getAddress());
            preparedStatement.setString(6, profile.getCity());
            preparedStatement.setString(7, profile.getState());
            preparedStatement.setString(8, profile.getZip());
            preparedStatement.setInt(9, profile.getUserId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Profile updated for user_id: {}", profile.getUserId());
            }
            else {
                logger.warn("No profile updated — user_id not found: {}", profile.getUserId());
            }

        } catch (SQLException e) {
            logger.error("Error updating profile for user_id: {}", profile.getUserId(), e);

//            e.printStackTrace();
        }


    }

    private Profile mapRow(ResultSet row) throws SQLException
    {
        int userId = row.getInt("user_id");
        String firstName = row.getString("first_name");
        String lastName = row.getString("last_name");
        String phone = row.getString("phone");
        String email = row.getString("email");
        String address = row.getString("address");
        String city = row.getString("city");
        String state = row.getString("state");
        String zip = row.getString("zip");

        return new Profile(userId, firstName, lastName, phone, email, address, city, state, zip);

    }
}
