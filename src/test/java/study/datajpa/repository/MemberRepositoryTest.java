package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  TeamRepository teamRepository;
  @PersistenceContext
  EntityManager em;

  @Test
  void testMember() {
    Member member = new Member("memberA");
    Member savedMember = memberRepository.save(member);

    Member findMember = memberRepository.findById(savedMember.getId()).get();

    assertThat(findMember.getId()).isEqualTo(member.getId());
    assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    assertThat(findMember).isEqualTo(member);
  }

  @Test
  public void basicCRUD() {
    Member member1 = new Member("member1");
    Member member2 = new Member("member2");

    memberRepository.save(member1);
    memberRepository.save(member2);

    // 단건 조회 검증
    Member findMember1 = memberRepository.findById(member1.getId()).get();
    Member findMember2 = memberRepository.findById(member2.getId()).get();

    assertThat(findMember1).isEqualTo(member1);
    assertThat(findMember2).isEqualTo(member2);

    // 리스트 조회 검증
    List<Member> all = memberRepository.findAll();
    assertThat(all.size()).isEqualTo(2);

    // 카운트 검증
    long count = memberRepository.count();
    assertThat(count).isEqualTo(2);

    //삭제 검증
    memberRepository.delete(member1);
    memberRepository.delete(member2);

    long deletedCount = memberRepository.count();
    assertThat(deletedCount).isEqualTo(0);

  }

  @Test
  public void findByUsernameAndAgeGreaterThen() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

    assertThat(result.get(0).getUsername()).isEqualTo("AAA");
    assertThat(result.get(0).getAge()).isEqualTo(20);
    assertThat(result.size())
            .isEqualTo(1);

  }

  @Test
  void findHeeloBy() {
    List<Member> helloBy = memberRepository.findHelloBy();
  }

  @Test
  void testQuery() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> user1 = memberRepository.findUser("AAA", 10);
    assertThat(user1.get(0)).isEqualTo(m1);
  }

  @Test
  void fineUsernameList() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<String> userNameList = memberRepository.findUserNameList();

    assertThat(userNameList.size()).isEqualTo(2);
  }

  @Test
  void findMemberDto() {
    Team team = new Team("teamA");
    teamRepository.save(team);

    Member m1 = new Member("AAA", 10);
    memberRepository.save(m1);
    m1.setTeam(team);

    List<MemberDto> memberDto = memberRepository.findMemberDto();
    for (MemberDto dto : memberDto) {
      System.out.println("dto = " + dto);
    }
  }

  @Test
  void findByNames() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
    for (Member member : result) {
      System.out.println("member = " + member);
    }
  }

  @Test
  void returnType() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> aaa = memberRepository.findByUsername("AAA");
    System.out.println("aaa = " + aaa);

    Optional<Member> aaa1 = memberRepository.findOptionalByUsername("AAA");
    System.out.println("aaa1 = " + aaa1);

    Member aaa2 = memberRepository.findMemberByUsername("AAA");
    System.out.println("aaa2 = " + aaa2);
  }

  @Test
  void paging() {
    // given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
    int age = 10;
    // when
    Page<Member> page = memberRepository.findByAge(age, pageRequest);

    Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));


    // then
    List<Member> content = page.getContent();

    assertThat(content.size()).isEqualTo(3);
    // 전체 카운터 갯수
    assertThat(page.getTotalElements()).isEqualTo(5);
    // page 번호
    assertThat(page.getNumber()).isEqualTo(0);
    // 전체 page 갯수
    assertThat(page.getTotalPages()).isEqualTo(2);
    // 첫 페이지 여부 확인
    assertThat(page.isFirst()).isTrue();
    // 다음 페이지 여부 확인
    assertThat(page.hasNext()).isTrue();

  }

  @Test
  public void bulkUpdate() {

    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 19));
    memberRepository.save(new Member("member3", 20));
    memberRepository.save(new Member("member4", 21));
    memberRepository.save(new Member("member5", 40));

    //when
    int resultCount = memberRepository.bulkAgePlus(20);
//    em.flush();
//    em.clear();

    List<Member> result = memberRepository.findByUsername("member5");
    Member member = result.get(0);
    System.out.println("member = " + member);

    //then
    assertThat(resultCount).isEqualTo(3);
  }

  @Test
  public void finMemberLazy() {
    //given
    //member1 -> teamA
    //member2 -> teamB

    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    teamRepository.save(teamA);
    teamRepository.save(teamB);
    Member member1 = new Member("member1", 10, teamA);
    Member member2 = new Member("member2", 10, teamB);
    memberRepository.save(member1);
    memberRepository.save(member2);

    em.flush();
    em.clear();

    //when
    List<Member> members = memberRepository.findAll();
    for (Member member : members) {
      System.out.println("member = " + member.getUsername());
      System.out.println("member.teamClass = " + member.getTeam().getClass());
      System.out.println("member.team = " + member.getTeam().getName());

    }


  }
  @Test
  public void queryHint() {

    //given
    Member member1 = memberRepository.save(new Member("member1", 10));
    em.flush();
    em.clear();

    //when
    Member findMember = memberRepository.findReadOnlyByUsername("member1");
    findMember.setUsername("member2");

    em.flush();
  }

  @Test
  public void lock() {

    //given
    Member member1 = memberRepository.save(new Member("member1", 10));
    em.flush();
    em.clear();

    //when
    List<Member> result = memberRepository.findLockByUsername("member1");

    em.flush();

  }



}
