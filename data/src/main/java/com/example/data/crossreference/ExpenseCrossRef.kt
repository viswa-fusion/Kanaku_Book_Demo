package com.example.data.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ExpenseType

@Entity(
    primaryKeys = ["expenseId","associatedId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expenseId"])]
)
data class ExpenseCrossRef(
    val expenseType: ExpenseType,
    val associatedId: Long,
    val expenseId: Long,
)


/*
@Dao
interface ExpenseDao {

}

@Dao
interface SplitDao {

}


this is my expense dao and split dao in android kotlin i need efficient query for using this

@Entity(tableName = "users", indices = [Index(value = ["phone"], unique = true)])
data class UserEntity(
    val name: String,
    val phone: Long,
    val password: String,
    val amountToGet: Double = 0.0,
    val amountToGive: Double = 0.0,
){
    @PrimaryKey(autoGenerate = true)
    var userId: Long = 0L
}

@Entity(
    tableName = "groups"
)
data class GroupEntity(
    val groupName: String,
    val createdBy: Long,
    val lastActive: Long,

    @PrimaryKey(autoGenerate = true)
    val groupId: Long = 0,
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["spenderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spenderId"])]
)
data class ExpenseEntity(
    val spenderId: Long,
    val amount: Double,
    val description: String?,
    val date: Long,


    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0
)


@Entity
data class SplitEntity(
    val splitUserId: Long,
    val splitAmount: Double,
    val paidStatus: PaidStatus,

    @PrimaryKey(autoGenerate = true)
    val splitId: Long = 0
)


@Entity(
    primaryKeys = ["user1Id","user2Id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["user1Id"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["user2Id"]
        )
    ],
    indices = [Index(value = ["user1Id"]),Index(value = ["user2Id"])]
)
data class FriendsConnectionCrossRef(
    val user1Id: Long,
    val user2Id: Long,

    @PrimaryKey(autoGenerate = true)
    val connectionId: Long = 0
)

@Entity(
    primaryKeys = ["groupId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        )
    ],
    indices = [Index(value = ["groupId"]), Index(value = ["userId"])]
)
data class GroupMemberCrossRef(
    val groupId: Long,
    val userId: Long
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expenseId"])]
)
data class ExpenseCrossRef(
    val expenseType: ExpenseType,
    val belongingId: Long,
    val expenseId: Long,
)
note: belongingId refers either groupId or Friends connectionId (also give the better naming for it)

@Entity(
    primaryKeys = ["expenseId","splitId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"]
        ),
        ForeignKey(
            entity = SplitEntity::class,
            parentColumns = ["splitId"],
            childColumns = ["splitId"]
        )
    ],
    indices = [Index(value = ["expenseId"]),Index(value = ["splitId"])]
)
data class SplitExpenseCrossRef(
    val expenseId: Long,
    val splitId: Long,
)

enum class ExpenseType {
    GroupExpense, FriendsExpense
}

relation :
data class ExpenseWithSplit(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "expenseId",
        entityColumn = "splitId",
        associateBy = Junction(SplitExpenseCrossRef::class)
    ) val splits: List<SplitEntity>
)

models:-
data class ExpenseData(
    val expenseId: Long,
    val spenderId: UserProfileSummary,
    val totalAmount: Double,
    val date: Long,
    val note: String?,
)

now i want the query to

put expense of ExpenseEntity it returns id
put split of SplitEntity it returns id
put splitExpenseCrossRef with expenseId and List<splitId>
put ExpenseCrossRef with ExpenseType and belongingId(is either groupId or friends connectionId) and foreign key that expenseId

get list of ExpenseEntity by ExpenseType and belongingId return list of ExpenseEntity with (userEntity by spenderId)
get list of split by expenseId return list of splitEntity with (userEntity by splitUserId)


give full code
 */