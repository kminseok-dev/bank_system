package view;

import java.util.List;

import dto.AccountCreateResponseDto;

public class Bankview {

	public static void menuDisplay() {
		System.out.println("\n===== 뱅킹 시스템 =====");
		System.out.println("1. 계좌 개설");
		System.out.println("2. 계좌번호로 조회");
		System.out.println("3. 전체 계좌 조회");
		System.out.println("4. 계좌 이체");
		System.out.println("5. 입금");
		System.out.println("6. 출금");
		System.out.println("7. 계좌 해지");
		System.out.println("99. 종료");
		System.out.print("메뉴 선택 >> ");
	}

	// 이체 결과 메시지 출력
	public static void printTransferMessage(int result) {
		switch (result) {
			case 1 -> System.out.println("> 이체가 정상적으로 완료되었습니다.");
			case -1 -> System.out.println("! 출금 계좌를 찾을 수 없습니다.");
			case -2 -> System.out.println("! 입금 계좌를 찾을 수 없습니다.");
			case -3 -> System.out.println("! 출금 계좌의 잔액이 부족합니다.");
			case -4 -> System.out.println("! 출금 계좌와 입금 계좌가 동일합니다.");
			case -5 -> System.out.println("! 이체 금액은 0보다 커야 합니다.");
			default -> System.out.println("! 이체 처리 중 오류가 발생했습니다.");
		}
	}

	public static void printMessage(String taskName, int result) {
		if (result > 0) {
			System.out.println(taskName + " 성공");
		} else {
			System.out.println(taskName + " 실패");
		}
	}

	public static void printAccount(AccountCreateResponseDto account) {
		if (account == null) {
			System.out.println("조회된 계좌가 없습니다.");
			return;
		}

		System.out.println("\n[계좌 정보]");
		System.out.println("계좌 ID: " + account.getAccountId());
		System.out.println("회원 ID: " + account.getMemberId());
		System.out.println("고객명: " + account.getCustomerName());
		System.out.println("계좌번호: " + account.getAccountNumber());
		System.out.println("계좌 이름: " + account.getAccountName());
		System.out.println("잔액: " + account.getBalance());
		System.out.println("개설일시: " + account.getCreatedAt());
	}

	public static void printAccount(List<AccountCreateResponseDto> aList) {
		if (aList == null || aList.isEmpty()) {
			System.out.println("조회된 계좌가 없습니다.");
			return;
		}

		for (AccountCreateResponseDto account : aList) {
			printAccount(account);
		}
	}

}
