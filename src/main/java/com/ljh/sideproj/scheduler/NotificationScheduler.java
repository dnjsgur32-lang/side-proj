package com.ljh.sideproj.scheduler;

import com.ljh.sideproj.dto.KamcoBid;
import com.ljh.sideproj.dto.UserAlert;
import com.ljh.sideproj.mapper.KamcoBidMapper;
import com.ljh.sideproj.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NotificationScheduler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private KamcoBidMapper kamcoBidMapper;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkDeadlines() {
        try {
            System.out.println("마감 임박 알림 체크 시작");
            
            List<UserAlert> alerts = notificationService.getActiveAlerts();
            if (alerts == null || alerts.isEmpty()) {
                System.out.println("활성 알림 없음");
                return;
            }

            for (UserAlert alert : alerts) {
                if ("DEADLINE".equals(alert.getAlertType()) && alert.getPbctNo() != null) {
                    checkBidDeadline(alert);
                }
            }
            System.out.println("마감 임박 알림 체크 완료");
        } catch (Exception e) {
            System.err.println("마감 알림 체크 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void checkPriceChanges() {
        try {
            System.out.println("가격 변동 체크 시작");
            
            List<UserAlert> alerts = notificationService.getActiveAlerts();
            if (alerts == null || alerts.isEmpty()) {
                return;
            }

            for (UserAlert alert : alerts) {
                if ("PRICE".equals(alert.getAlertType()) && alert.getPbctNo() != null) {
                    // TODO: 가격 변동 체크 로직 구현
                }
            }
            System.out.println("가격 변동 체크 완료");
        } catch (Exception e) {
            System.err.println("가격 변동 체크 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 1800000)
    public void checkNewBids() {
        try {
            System.out.println("신규 입찰 체크 시작");
            
            List<UserAlert> alerts = notificationService.getActiveAlerts();
            if (alerts == null || alerts.isEmpty()) {
                return;
            }

            for (UserAlert alert : alerts) {
                if ("NEW".equals(alert.getAlertType())) {
                    // TODO: 신규 입찰 체크 로직 구현
                }
            }
            System.out.println("신규 입찰 체크 완료");
        } catch (Exception e) {
            System.err.println("신규 입찰 체크 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkBidDeadline(UserAlert alert) {
        try {
            if (alert == null || alert.getPbctNo() == null) {
                return;
            }
            
            List<KamcoBid> bids = kamcoBidMapper.findWithFilters(null, null, null, null, null, 0, 1000);
            if (bids == null || bids.isEmpty()) {
                return;
            }

            for (KamcoBid bid : bids) {
                if (alert.getPbctNo().equals(bid.getPbctNo()) && bid.getPbctEndDt() != null && !bid.getPbctEndDt().isEmpty()) {
                    try {
                        LocalDate endDate = LocalDate.parse(bid.getPbctEndDt(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                        LocalDate targetDate = LocalDate.now().plusDays(alert.getAlertDaysBefore() != null ? alert.getAlertDaysBefore() : 0);

                        if (endDate.equals(targetDate)) {
                            String message = String.format("%s 입찰이 %d일 후 마감됩니다.",
                                    bid.getCltrNm(), alert.getAlertDaysBefore());
                            notificationService.createNotification(
                                    alert.getUserId(), bid.getPbctNo(), "DEADLINE", message);
                        }
                    } catch (Exception e) {
                        System.err.println("날짜 파싱 오류 (pbctNo: " + bid.getPbctNo() + "): " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("마감 체크 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}