# language: ru
Функционал: Лента агрегированных логов приложения. Фильтрация по severity

Сценарий: При смене severity=warning на странице показываются error и warning записи

Допустим приложение "SeverityWarningTest" залогировало warning "warning log"
И приложение "SeverityWarningTest" залогировало ошибку "error log"
Когда я захожу на страницу приложения "SeverityWarningTest"
И я меняю severity на "warning"
Тогда я вижу лог "error log"
И я вижу лог "warning log"

Сценарий: При смене severity=error на странице показываются только error записи

Допустим приложение "SeverityErrorTest" залогировало warning "warning log"
И приложение "SeverityErrorTest" залогировало ошибку "error log"
Когда я захожу на страницу приложения "SeverityErrorTest"
И я меняю severity на "error"
Тогда я вижу лог "error log"
И я не вижу лог "warning log"