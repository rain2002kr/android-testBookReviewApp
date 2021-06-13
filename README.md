# android-testBookReviewApp

# 2021-0522 -0526일
  메인 activity 
  step 1 : retrofit 이용하여 interpark api 호출 하여 데이터 가져오기
    0. PostMAN 으로 API 테스트   
    1. dependency 추가 
    2. 메니페스트 권한 추가 INTERNET 권한 HTTPS 와 HTTP 통신 할거면 허용.
    3. retrofit DTO 데이터 클래스 만들기
        1. API 키 저장 XML 파일 만들기
    4. MainActivity 에서 레트로핏 빌더 패턴으로 만들어서 호출하기 
        1. log 찍어보기 
   
  step 2 : 가져온 데이터를 recycler view 에 뿌리기 
  완료, interpark open API 가져올때, dto에 item 잘못 적어놔서 고생했음. 
    
# 2021-0528 

   Room 검색어 저장 및 삭제 기능 확인

# 2021-0530
    검색어 저장 및 키워드 이용해서 북 API 가져오기 
    History Recycler view show and hide 
    detail 뷰 구성  
    
# 2021----- 
  메인 activity 
  target1 : 최근 검색어 Room 이용하여, 내부 저장하고 삭제 가능 하도록 할 것  
  target2 : 검색 기능 이용하여, inter park open API 에 책 검색 기록 가져 올것 
  target3 : 가져온 데이터 기반으로 새로운 리싸이클러뷰 보여주고, 다른 리싸이클러뷰는 숨길것 
  target4 : 리싸이클러뷰 터치하면 detail Activity 띄우면서 보여 줄것 
  
# 2021-0613 
  annotationProcessor  'com.github.bumptech.glide:compiler:4.12.0' 추가 했음. 
  최근 검색어 누르면 바로 검색 되는 기능 추가 
  History Adapter 에 bind.root.setOnclickListener { 이 안에 함수 넣기 }
  MainActivity 에서 adapter 안에다가 다음 함수 호출 후, 받아온 값으로 처리  
  
# 의문 사항 
    분명 UI 작업 할땐, runOnUiThread 에서 해야 한다고 했는데, 반대로 해야 에러가 발생 하지 않았다. 
    
  
 
 
