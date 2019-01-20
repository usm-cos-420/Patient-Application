package edu.usm.cos420.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.usm.cos420.domain.Patient;
import edu.usm.cos420.domain.Result;

public class CloudSqlDao implements PatientDao {
	String dbUrl;

	public CloudSqlDao(String dbUrl) throws SQLException {
		this.dbUrl = dbUrl;
		System.out.println("Cloud SQL Dao: " + this.dbUrl);
		this.createPatientTable();
	}

	public void createPatientTable() throws SQLException {
		try(Connection conn = DriverManager.getConnection(this.dbUrl)){
			String createDbQuery =  "CREATE TABLE IF NOT EXISTS patients ( id SERIAL PRIMARY KEY, "
					+ "firstName VARCHAR(255), lastName VARCHAR(255), birthDate DATE)";

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(createDbQuery);

			if(conn != null)
				conn.close();
		}
	}

	@Override
	public Long createPatient(Patient patient) throws SQLException {
		Long id = 0L;
		final String createPatientString = "INSERT INTO patients "
				+ "(firstName, lastName, birthDate) "
				+ "VALUES (?, ?, ?)";
		try (Connection conn = DriverManager.getConnection(this.dbUrl);
				final PreparedStatement createPatientStmt = conn.prepareStatement(createPatientString,
						Statement.RETURN_GENERATED_KEYS)) {
			createPatientStmt.setString(1, patient.getFirstName());
			createPatientStmt.setString(2, patient.getLastName());
			createPatientStmt.setDate(3, (Date) patient.getBirthDate());

			createPatientStmt.executeUpdate();
			try (ResultSet keys = createPatientStmt.getGeneratedKeys()) {
				keys.next();
				id = keys.getLong(1);
			}
		}
		
		return id;
	}

	@Override
	public Patient readPatient(Long patientId) throws SQLException {
		final String readBookString = "SELECT * FROM patients WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(this.dbUrl);
				PreparedStatement readBookStmt = conn.prepareStatement(readBookString)) {
			readBookStmt.setLong(1, patientId);
			try (ResultSet keys = readBookStmt.executeQuery()) {
				keys.next();
				Patient patient = new Patient();
				patient.setId(keys.getInt(1));
				patient.setFirstName(keys.getString(2));
				patient.setLastName(keys.getString(3));
				patient.setBirthDate(keys.getDate(4));

				return patient;
			}
		}
	}

	@Override
	public void updatePatient(Patient patient) throws SQLException {
		final String updateBookString = "UPDATE patients SET firstName = ?, lastName = ?, birthDate = ?  WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(this.dbUrl);
				PreparedStatement updateBookStmt = conn.prepareStatement(updateBookString)) {
			updateBookStmt.setString(1, patient.getFirstName());
			updateBookStmt.setString(2, patient.getLastName());
			updateBookStmt.setDate(3, (Date) patient.getBirthDate());
			updateBookStmt.setLong(4, patient.getId());
			updateBookStmt.executeUpdate();
		}

	}

	@Override
	public void deletePatient(Long patientId) throws SQLException {
		final String deleteBookString = "DELETE FROM patients WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(this.dbUrl);
				PreparedStatement deleteBookStmt = conn.prepareStatement(deleteBookString)) {
			deleteBookStmt.setLong(1, patientId);
			deleteBookStmt.executeUpdate();
		}
	}

	@Override
	public Result<Patient> listPatients(String cursor) throws SQLException {
		int offset = 0;
		int totalNumRows = 0;
		if (cursor != null && !cursor.equals("")) {
			offset = Integer.parseInt(cursor);
		}
		
		final String listPatientsString = "SELECT id, firstName, lastName, birthDate, count(*) OVER() AS total_count FROM patients ORDER BY lastName, firstName ASC "
				+ "LIMIT 10 OFFSET ?";
		try (Connection conn = DriverManager.getConnection(this.dbUrl);
				PreparedStatement listPatientStmt = conn.prepareStatement(listPatientsString)) {
			System.out.println("Querying Patients Table: " + listPatientsString );
			listPatientStmt.setInt(1, offset);
			List<Patient> resultPatients = new ArrayList<>();

			try (ResultSet rs = listPatientStmt.executeQuery()) {
				while (rs.next()) {
					Patient patient = new Patient();
					patient.setId(rs.getInt(1));
					patient.setFirstName(rs.getString(2));
					patient.setLastName(rs.getString(3));
					patient.setBirthDate(rs.getDate(4));

					resultPatients.add(patient);

					totalNumRows = rs.getInt("total_count");
				}
			}

			if (totalNumRows > offset + 10) {
				return new Result<>(resultPatients, Integer.toString(offset + 10));
			} else {
				return new Result<>(resultPatients);
			}
			
			
		}
	}

}

