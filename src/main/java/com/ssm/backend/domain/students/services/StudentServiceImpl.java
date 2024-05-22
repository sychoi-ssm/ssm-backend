package com.ssm.backend.domain.students.services;

import com.ssm.backend.domain.students.dto.StudentMst;
import com.ssm.backend.domain.students.dto.StudentProfile;
import com.ssm.backend.domain.students.dto.StudentSurveyMst;
import com.ssm.backend.domain.students.dto.StudentSurveyScoreHis;
import com.ssm.backend.domain.students.mappers.StudentMapper;
import com.ssm.backend.global.annotations.Audit;
import com.ssm.backend.global.common.AuditUtil;
import com.ssm.backend.global.exceptions.ErrorCode;
import com.ssm.backend.global.exceptions.SsmException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {
    private final StudentMapper studentMapper;
    @Override
    public void createStudentMst(StudentMst studentMst){}

    public StudentMst getStudentMstWithStudentId(long studentId) {
        return getStudentMstWithStudentId(StudentMst.builder().studentId(studentId).build());
    }
    @Override
    public StudentMst getStudentMstWithStudentId(StudentMst studentMst){
        return studentMapper.selectStudentMstWithStudentId(studentMst)
                .orElseThrow(SsmException.supplier(ErrorCode.STUDENT_NOT_FOUND, "해당 studentId의 학생을 찾을 수 없습니다."));
    }
    @Override
    public StudentMst createOneStudent(StudentMst studentMst) {
        StudentSurveyMst studentSurveyMst = studentMst.getSurvey();
        StudentProfile studentProfile = studentMst.getProfile();
        if (studentSurveyMst == null) {
            throw SsmException.from(ErrorCode.BAD_STUDENT_BODY, "Survey는 null 일 수 없습니다.");
        }
        if (studentProfile == null) {
            throw SsmException.from(ErrorCode.BAD_STUDENT_BODY, "Profile은 null 일 수 없습니다.");
        }
        studentMapper.insertOneStudentMst(studentMst);

        Long studentId = studentMst.getStudentId();

        // Insert Student Profile
        studentProfile.setStudentId(studentId);
        studentMapper.insertOneStudentProfile(studentProfile);

        // Insert Student Survey Mst
        studentSurveyMst.setStudentId(studentId);
        studentMapper.insertOneStudentSurveyMst(studentSurveyMst);

        // Insert Student Survey Score History
        for (StudentSurveyScoreHis scoreHis: studentSurveyMst.getScoreHistories()) {
            scoreHis.setStudentId(studentId);
            studentMapper.insertOneStudentSurveyScoreHis(scoreHis);
        }
        studentMst.setIncludeSurvey(true);
        studentMst.setIncludeProfile(true);
        return getStudentMstWithStudentId(studentMst);
    }
    @Override
    public StudentProfile getStudentProfile(StudentProfile studentProfile) {
        return studentMapper.selectOneStudentProfile(studentProfile)
                .orElseThrow(SsmException.supplier(ErrorCode.STUDENT_NOT_FOUND, "해당 StudentId와 일치하는 학생 프로필을 찾을 수 없습니다."));
    }

    @Override
    public StudentProfile updateStudentProfile(StudentProfile studentProfile) {
        getStudentMstWithStudentId(studentProfile.getStudentId()); // 학생 마스터 존재여부 체크
        getStudentProfile(studentProfile); // 학생 프로필 존재여부 체크
        studentMapper.cloneStudentProfileIntoHis(studentProfile);
        studentMapper.updateStudentProfile(studentProfile);
        return getStudentProfile(studentProfile);
    }
}
