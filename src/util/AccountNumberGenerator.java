package util;

import java.util.concurrent.ThreadLocalRandom;

public class AccountNumberGenerator {

	private static final String BANK_CODE = "213";
	
	public String generateAccNum() {
		
		return BANK_CODE + String.valueOf(ThreadLocalRandom.current().nextInt(100000000,1000000000));

	}
	
}
