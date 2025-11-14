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
@EnableScheduling
public class NotificationScheduler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private KamcoBidMapper kamcoBidMapper;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkDeadlines () {
        System.out.println("마감 임박 알림 체크 시작");

        List<UserAlert> alerts = notificationService.getActiveAlerts();

        for (UserAlert alert : alerts) {
            if ("DEADLINE".equals(alert.getAlertType()) && alert.getPbctNo() != null) {
                checkBidDeadline(alert);
            }
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void checkPriceChanges () {
        System.out.println("가격 변동 체크 시작");

        List<UserAlert> alerts = notificationService.getActiveAlerts();

        for (UserAlert alert : alerts) {
            if ("PRICE".equals(alert.getAlertType()) && alert.getPbctNo() != null) {
            }
        }
    }

    @Scheduled(fixedRate = 1800000)
    public void checkNewBids () {
        System.out.println("신규 입찰 체크 시작");

        List<UserAlert> alerts = notificationService.getActiveAlerts();

        for (UserAlert alert : alerts) {
            if ("NEW".equals(alert.getAlertType())) {
            }
        }
    }

    private void checkBidDeadline (UserAlert alert) {
        try {
            List<KamcoBid> bids = kamcoBidMapper.findWithFilters(null, null, null, null, null, 0, 1000);

            for (KamcoBid bid : bids) {
                if (alert.getPbctNo().equals(bid.getPbctNo()) && bid.getPbctEndDt() != null) {
                    LocalDate endDate = LocalDate.parse(bid.getPbctEndDt(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    LocalDate targetDate = LocalDate.now().plusDays(alert.getAlertDaysBefore());

                    if (endDate.equals(targetDate)) {
                        String message = String.format("%s 입찰이 %d일 후 마감됩니다.",
                                bid.getCltrNm(), alert.getAlertDaysBefore());
                        notificationService.createNotification(
                                alert.getUserId(), bid.getPbctNo(), "DEADLINE", message);

                    }
                }
            }
        } catch (Exception e) {
            System.err.println("마감 체크 오류: " + e.getMessage());
        }
    }
}