package es.eina.sql.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLParameterString implements ISQLParameter{
	
	private String param = "";
	
	public SQLParameterString(String param) {
		this.param = param;
	}
	
	public String getParam(){
		return param;
	}
	
	@Override
	public PreparedStatement handleParameter(int index, PreparedStatement p) {
		try {
			p.setString(index, getParam());
		} catch (SQLException e) {
			System.out.println("Couldn't set param " + index + " with value " + getParam());
			e.printStackTrace();
		}
		return p;
	}
	

}
