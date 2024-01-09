package member.jdbc

import extension.TableRowMapperExtension
import member.Member
import member.MemberRepository
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository

@Repository
class MemberJdbcRepositoryImpl(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) : MemberRepository {

    fun findAllByIdIn(ids: List<Long>): List<Member> {
        val sql = String.format(
            "select * from %s where id in (:ids)",
            TABLE
        )
        val params = MapSqlParameterSource()
            .addValue("ids", ids)

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER)
    }

    fun findById(id: Long): Member? {
        val sql = String.format(
            "select * from %s where id = :id",
            TABLE
        )
        val params = MapSqlParameterSource()
            .addValue("id", id)

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER).firstOrNull()
    }

    fun save(member: Member): Member {
        return if (member.isMemberNotInitialized()) {
            insert(member)
        } else {
            update(member)
        }
    }

    private fun insert(member: Member): Member {
        val jdbcInsert = SimpleJdbcInsert(namedParameterJdbcTemplate.jdbcTemplate)
            .withTableName(TABLE)
            .usingGeneratedKeyColumns("id")

        val params = BeanPropertySqlParameterSource(member)
        val id = jdbcInsert.execute(params).toLong()

        return member.copy(
            id = id,
            email = member.email,
            nickName = member.nickName,
            birthDay = member.birthDay,
            createdAt = member.createdAt,
        )
    }

    private fun update(member: Member): Member {
        val sql = String.format(
            "update %s set email = :email, nickname = :nickname, birthday = :birthday where id = :id",
            TABLE
        )

        val params = BeanPropertySqlParameterSource(member)
        namedParameterJdbcTemplate.update(sql, params)
        return member
    }

    companion object {
        const val TABLE = "member"

        val ROW_MAPPER = TableRowMapperExtension.rowMapper { rs, _ ->
            Member(
                id = rs.getLong("id"),
                email = rs.getString("email"),
                nickName = rs.getString("nickname"),
                birthDay = rs.getDate("birthday").toLocalDate(),
                createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            )
        }
    }
}
