```
# Project Structure

---

dev.ajith.khatape/
├── data/
│   ├── local/
│   │   ├── entity/              // Room entities: FriendEntity, TransactionEntity, GroupEntity(optional)
│   │   ├── dao/                 // DAOs: FriendDao, TransactionDao, FriendSummaryDao
│   │   ├── converter/           // Converters: InstantConverter, TransactionDirectionConverter
│   │   └── AppDatabase.kt       // Room DB setup
│   └── repository/              // Data access layer (TransactionRepository, FriendRepository)
│
├── domain/
│   ├── model/                   // UI-friendly models: DebtStatus, SplitType
│   │   ├── Friend         
│   │   ├── FriendSummary
│   │   ├── Transaction
│   │   └── TransactionDirection
│   ├── usecase/                 // Business logic: AddExpenseUseCase, SettleDebtUseCase
│   │   ├── AddTransactionUseCase
│   │   ├── CreateFriendUseCase
│   │   ├── GetFriendSummariesUseCase
│   │   ├── GetFriendSummaryByIdUseCase
│   │   └── SettleTransactionUseCase
│   └── mapper/                  // Entity ↔ Domain model converters
│       ├── FriendMapper
│       ├── FriendSummaryMapper
│       └── TransactionMapper
│
├── ui/
│   ├── theme/                   // Colors, typography, spacing tokens
│   ├── components/              // Reusable Composables: DebtCard, FriendChip
│   │   ├── BalanceCard
│   │   ├── CreateKhataDialog
│   │   └── AnalyticsCard
│   ├── screen/
│   │   ├── dashboard/           // DashboardScreen.kt
│   │   ├── frienddetail/        // FriendDetailScreen.kt
│   │   ├── friends/             // Friends.kt
│   │   └── settings/            // SettingsScreen.kt
│   └── navigation/              // NavHost, route definitions
│
├── di/                          // Hilt modules: DatabaseModule, RepositoryModule
├── utils/                       // Extensions, constants, helpers
├── MainActivity.kt              // Entry point
└── KhataPe.kt                   // App-level Composable with NavHost
```