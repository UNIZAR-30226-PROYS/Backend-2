package es.eina.sql.parameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLParameterBoolean implements ISQLParameter{
	
	private boolean param = false;
	
	public SQLParameterBoolean(boolean param) {
		this.param = param;
	}
	
	public boolean getParam(){
		return param;
	}
	
	@Override
	public PreparedStatement handleParameter(int index, PreparedStatement p) {
		try {
			p.setBoolean(index, getParam());
		} catch (SQLException e) {
			System.out.println("Couldn't set param " + index + " with value " + getParam());
			e.printStackTrace();
		}
		return p;
	}

}
