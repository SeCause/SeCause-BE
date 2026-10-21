# 분석 코드 통계 콜백 계약 (#60)

## 성공 콜백

`POST /api/internal/analyses/{analysisId}/result`에 선택 객체 `codeDetails`를 추가한다.

```json
{
  "analysisId": 1,
  "repositoryId": 21,
  "status": "COMPLETED",
  "findings": [],
  "summary": {"totalCount": 0},
  "codeDetails": {
    "fileCount": 42,
    "lineCount": 1000,
    "languages": ["JavaScript", "TypeScript"]
  }
}
```

- `fileCount`: 취약점 유무와 무관한 전체 분석 대상 파일 수. 0 이상의 정수.
- `lineCount`: 같은 분석 대상 파일 집합의 전체 라인 수. 0 이상의 정수(Long).
- `languages`: 같은 파일 집합의 언어 목록. 빈 배열 허용. null/공백 원소 불가, 항목당 최대 100자. 저장 시 앞뒤 공백 제거, 중복 제거, 정렬.
- 객체를 보내면 세 필드는 모두 필수. 잘못된 값은 HTTP 400으로 거절한다.
- 객체 생략 또는 null이면 기존 통계를 유지한다. 기존 분석 서버 콜백도 계속 처리한다.
- 0/빈 배열을 명시하면 유효한 빈 분석 결과로 저장한다.
- 종료된 분석에 대한 중복 콜백으로 통계를 다시 덮어쓰지 않는다.
- `summary.totalCount`는 이슈 수이며 파일 수로 사용하지 않는다.

## 저장 및 조회

파일 수·라인 수는 기존 repositories 컬럼을 사용한다. 언어는 신규 nullable JSONB `repositories.languages`에 저장하며 schema.sql의 멱등 ALTER 문으로 추가한다.
레포지토리 목록과 대시보드는 모두 이 전체 통계를 반환한다. 언어를 수정 예시에서 추정하지 않는다. 아직 통계가 없는 레포지토리는 기존 응답 형식에 맞춰 언어를 빈 배열로 반환한다.

## 분석 서버 연동 및 기존 데이터

현재 로컬 SeCause-Analysis의 AnalysisSummary는 totalCount만 제공하며 코드 통계 집계/전송은 없다. 분석 서버에서 clone된 요청 브랜치의 파일을 집계하고 위 객체를 전송하는 후속 작업이 필요하다. .git, 바이너리, 생성물, 의존성 디렉터리의 제외 기준 및 텍스트 라인/언어 판별 기준은 분석 서버 담당자와 확정해야 한다. 백엔드는 전달된 통계를 보존하며 별도로 추정하지 않는다.

백엔드 배포만으로 과거 분석의 0 값이 채워지지는 않는다. 분석 서버 연동 후 새 분석을 요청하거나 별도 통계 보정 작업이 필요하다. 이미 완료된 분석에 같은 성공 콜백을 재전송하는 방식은 중복 방지 정책에 따라 적용되지 않는다. 운영 데이터 일괄 변경은 이 작업에 포함하지 않는다.
