package service;

import java.util.List;

import dao.BankDao;
import dto.AccountCreateRequestDto;
import dto.AccountCreateResponseDto;
import dto.TransferRequestDto;
import util.AccountNumberGenerator;

public class Bankservice {
	
	BankDao bankDAO = new BankDao();
	AccountNumberGenerator generator = new AccountNumberGenerator();
	
	// 1. 신규 계좌 개설 서비스
		public int insertService(AccountCreateRequestDto requestDto) {
			// 서비스 계층에서 계좌번호 생성기를 작동시킵니다.
			String newAccountNumber = generator.generateAccNum();
			
			// 생성된 계좌번호를 전달하여 BankDAO의 insert 메서드 호출
			int result = bankDAO.insertAccount(requestDto, newAccountNumber);
			return result;
		}
		
		// 2. 계좌번호로 단건 계좌 조회 서비스
		public AccountCreateResponseDto selectByAccountNumberService(String accountNumber) {
			AccountCreateResponseDto account = bankDAO.selectByAccountNumber(accountNumber);
			return account;
		}
		
		// 3. 전체 계좌 조회 서비스
		public List<AccountCreateResponseDto> selectAllService() {
			return bankDAO.selectAll();
		}

		// 4. 계좌 이체 서비스
		public int transferService(TransferRequestDto requestDto) {
			// 같은 계좌로의 이체 방지
			if (requestDto.getFromAccountNumber().equals(requestDto.getToAccountNumber())) {
				return -4; // 출금/입금 계좌 동일
			}
			// 이체 금액 유효성 검증
			if (requestDto.getAmount() == null || requestDto.getAmount() <= 0) {
				return -5; // 금액 오류
			}
			return bankDAO.transfer(requestDto);
		}

		// 5. 입금 서비스
		public int depositService(String accountNumber, Long amount) {
			validateAmount(amount, "입금");
			validateAccountNumber(accountNumber);

			AccountCreateResponseDto account = bankDAO.selectByAccountNumber(accountNumber);
			if (account == null) {
				throw new IllegalArgumentException("존재하지 않는 계좌번호입니다.");
			}

			return bankDAO.deposit(accountNumber, amount);
		}

		// 6. 출금 서비스
		public int withdrawService(String accountNumber, Long amount) {
			validateAmount(amount, "출금");
			validateAccountNumber(accountNumber);

			AccountCreateResponseDto account = bankDAO.selectByAccountNumber(accountNumber);
			if (account == null) {
				throw new IllegalArgumentException("존재하지 않는 계좌번호입니다.");
			}

			if (account.getBalance() < amount) {
				throw new IllegalArgumentException("잔액이 부족합니다.");
			}

			return bankDAO.withdraw(accountNumber, amount);
		}

		// 7. 계좌 해지 서비스 (잔액이 0일 때만 삭제 가능)
		public int closeService(String accountNumber) {
			validateAccountNumber(accountNumber);

			AccountCreateResponseDto account = bankDAO.selectByAccountNumber(accountNumber);
			if (account == null) {
				throw new IllegalArgumentException("존재하지 않는 계좌입니다.");
			}

			if (account.getBalance() != 0) {
				throw new IllegalArgumentException("잔액이 남아있어 해지할 수 없습니다. (남은 잔액: " + account.getBalance() + ")");
			}

			return bankDAO.closeAccount(accountNumber);
		}

		private void validateAmount(Long amount, String taskName) {
			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException(taskName + " 금액은 0보다 커야 합니다.");
			}
		}

		private void validateAccountNumber(String accountNumber) {
			if (accountNumber == null || accountNumber.isBlank()) {
				throw new IllegalArgumentException("계좌번호를 입력해 주세요.");
			}
		}
}
