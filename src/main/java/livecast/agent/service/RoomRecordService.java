package livecast.agent.service;

import livecast.agent.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class RoomRecordService {
//    private RoomRecordRepository recordRepository;
    private DateTimeUtil dateTimeUtil;

    @Autowired
    public RoomRecordService(//RoomRecordRepository recordRepository,
                             DateTimeUtil dateTimeUtil) {
//        this.recordRepository = recordRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

//    @Transactional
//    public OTLRoomRecord createRecord(String roomId, int activationId, OTLRoomRecordState state, ZoneId timeZone) {
//        final LocalDateTime now = LocalDateTime.now();
//        final String message = "OK";
//        final ZonedDateTime zoneNow = ZonedDateTime.now();
//        final LocalDateTime startTime = dateTimeUtil.toZoneLocalDateTime(zoneNow, timeZone);
//
//        final OTLRoomRecord newRecord = OTLRoomRecord.builder()
//                .roomId(roomId)
//                .activationId(activationId)
//                .recordId(0)
//                .state(state)
//                .recordStartDatetime(null)
//                .recordEndDatetime(null)
//                .downloadUrl(null)
//                .startDatetime(startTime)
//                .endDatetime(null)
//                .timeZone(timeZone)
//                .createdDatetime(now)
//                .updatedDatetime(now)
//                .build();
//
//        recordRepository.insertRecord(newRecord);
//
//        final OTLRoomRecordHistory newHistory = OTLRoomRecordHistory.builder()
//                .roomId(newRecord.getRoomId())
//                .activationId(newRecord.getActivationId())
//                .recordId(newRecord.getRecordId())
//                .historyId(0)
//                .state(state)
//                .message(message)
//                .createdDatetime(now)
//                .updatedDatetime(now)
//                .build();
//        recordRepository.insertRecordHistory(newHistory);
//
//        return newRecord;
//    }

//    @Transactional
//    public void updateRecordState(OTLRoomRecordStateUpdate update, ZoneId timeZone) {
//        final OTLRoomRecord record = OTLRoomRecord.builder()
//                .roomId(update.getRoomId())
//                .activationId(update.getActivationId())
//                .recordId(update.getRecordId())
//                .state(update.getState())
//                .recordStartDatetime(null)
//                .recordEndDatetime(null)
//                .downloadUrl(null)
//                .startDatetime(null)
//                .endDatetime(null)
//                .updatedDatetime(LocalDateTime.now())
//                .build();
//        if(update.getState().equals(OTLRoomRecordState.Recording)) {
//            final ZonedDateTime zoneNow = ZonedDateTime.now();
//            final LocalDateTime nowRoomDatetime = dateTimeUtil.toZoneLocalDateTime(zoneNow, timeZone);
//
//            record.setRecordStartDatetime(nowRoomDatetime);
//        }
//        if(update.getState().equals(OTLRoomRecordState.Recorded)) {
//            final ZonedDateTime zoneNow = ZonedDateTime.now();
//            final LocalDateTime nowRoomDatetime = dateTimeUtil.toZoneLocalDateTime(zoneNow, timeZone);
//
//            record.setRecordEndDatetime(nowRoomDatetime);
//        }
//        if(update.getState().equals(OTLRoomRecordState.Completed)) {
//            final ZonedDateTime zoneNow = ZonedDateTime.now();
//            final LocalDateTime nowRoomDatetime = dateTimeUtil.toZoneLocalDateTime(zoneNow, timeZone);
//
//            record.setEndDatetime(nowRoomDatetime);
//            record.setDownloadUrl(update.getDownloadUrl());
//        }
//
//        final OTLRoomRecordHistory recordHistory = OTLRoomRecordHistory.builder()
//                .roomId(update.getRoomId())
//                .activationId(update.getActivationId())
//                .recordId(update.getRecordId())
//                .state(update.getState())
//                .message(update.getMessage())
//                .build();
//
//        recordRepository.updateRecordState(record);
//        recordRepository.insertRecordHistory(recordHistory);
//    }
}
