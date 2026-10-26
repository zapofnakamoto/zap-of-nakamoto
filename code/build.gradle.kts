// Zap of Nakamoto's design in code: its animated mark, wordmark and components (White Wallet's docs/WHITE_LABEL.md,
// "Design"). Built into the app because this brand directory has a code/ module; it may use core:brand-api and
// core:brand-ui only.
plugins {
    id("whitewallet.brand-code")
}

android {
    namespace = "io.github.zapofnakamoto.design"
}
