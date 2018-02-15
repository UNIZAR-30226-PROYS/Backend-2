package es.eina.crypt;

public class Crypter {

	// Define the BCrypt VIP_WORKLOAD to use when generating password hashes. 10-31 is a valid value.
	private static final int VIP_WORKLOAD = 12;
	private static final int USER_WORKLOAD = 10;
	private static final int HASH_WORKLOAD = 5;

	/**
	 * This method can be used to generate a string representing an account password
	 * suitable for storing in a database. It will be an OpenBSD-style crypt(3) formatted
	 * hash string of length=60
	 * The bcrypt VIP_WORKLOAD is specified in the above static variable, a value from 10 to 31.
	 * A VIP_WORKLOAD of 12 is a very reasonable safe default as of 2013.
	 * This automatically handles secure 128-bit salt generation and storage within the hash.
	 * @param password_plaintext The account's plaintext password as provided during account creation,
	 *			     or when changing an account's password.
	 * @param highSecurity True if crypter should use higher security (2^12 rounds) or false if not (2^10 rounds).
	 * @return String - a string of length 60 that is the bcrypt hashed password in crypt(3) format.
	 */
	public static String hashPassword(String password_plaintext, boolean highSecurity) {
		String salt = BCrypt.gensalt(highSecurity ? VIP_WORKLOAD : USER_WORKLOAD);
		return BCrypt.hashpw(password_plaintext, salt);
	}

	/**
	 * This method can be used to verify a computed hash from a plaintext (e.g. during a login
	 * request) with that of a stored hash from a database. The password hash from the database
	 * must be passed as the second variable.
	 * @param password_plaintext The account's plaintext password, as provided during a login request
	 * @param stored_hash The account's stored password hash, retrieved from the authorization database
	 * @return boolean - true if the password matches the password of the stored hash, false otherwise
	 */
	public static boolean checkPassword(String password_plaintext, String stored_hash) {
		boolean password_verified;

		if(stored_hash == null || !stored_hash.startsWith("$2a$"))
			throw new IllegalArgumentException("Invalid hash provided for comparison");

		password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

		return password_verified;
	}

	public static String hashHash(String hash) {
		String salt = BCrypt.gensalt(HASH_WORKLOAD);
		return BCrypt.hashpw(hash, salt);
	}
}

