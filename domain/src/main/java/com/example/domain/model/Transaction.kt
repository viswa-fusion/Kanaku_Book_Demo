package com.example.domain.model

import com.example.domain.enum.PaidStatus

data class Transaction(
    val id: Long,
    val sender: UserData,
    val receiver: UserData,
    val amount: Double,
    val note: String,
    val date: Long
)
//
//data class SplitRequest1(
//    val id: Long,
//    val owner: UserData,
//    val amount: Double,
//    val members: List<UserData>
//)
//
//
//
//data class Split(
//    val id: Long,
//    val splitRequestId: Int,
//    val userData: UserData,
//    val amount: Double,
//    val status: PaidStatus
//)
//
//
//data class DbSplitRequest(
//    val id: Long,
//    val ownerId: Long,
//    val totalAmount: Double,
//    val groupId: Long,
//    val date: Long,
//
//    val receiverId: Long?,
//    val paidStatus: PaidStatus?,
//)
//
//data class spliting(
//    val id: Long,
//    val dbSplitRequestId: Long,
//    val userId: Long,
//    val splitAmount: Double,
//    val paidStatus: PaidStatus
//)
//
//
////data class DbRequest(
////    val id: Long,
////    val senderId: Long,
////    val receiverId: Long,
////    val amount: Double,
////    val commonId: Long,
////    val groupId: Long,
////    val paidStatus: PaidStatus,
////    val date: Long,
////)
//
//
//
//
//data class SplitRequest(
//    val requestId: Long,
//    val ownerId: Long,
//    val totalAmount: Double,
//    val groupId: Long,
//    val requestDate: Long,
//    val receiverId: Long?,
//    val paidStatus: PaidStatus
//)
//
//data class SplitTransaction(
//    val transactionId: Long,
//    val requestId: Long,
//    val userId: Long,
//    val splitAmount: Double,
//    val paidStatus: PaidStatus
//)
//
//
//
//
//data class SplitRequestEntity(
//    val requestId: Long,
//    val ownerId: Long,
//    val totalAmount: Double,
//    val groupId: Long,
//    val requestDate: Long,
//    val receiverId: Long?,
//    val paidStatus: PaidStatus
//)
//
//
//data class SplitTransactionEntity(
//    val transactionId: Long,
//    val requestId: Long,
//    val userId: Long,
//    val splitAmount: Double,
//    val paidStatus: PaidStatus
//)