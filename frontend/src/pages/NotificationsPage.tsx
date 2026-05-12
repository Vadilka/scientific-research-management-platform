import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  fetchNotificationSummary,
  fetchNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../api/client'
import type { NotificationItem } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { LoginRequired } from '../components/LoginRequired'

export function NotificationsPage() {
  const { t } = useTranslation()
  const { credentials, isAuthenticated } = useAuth()
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [error, setError] = useState<string | null>(null)

  const loadNotifications = useMemo(
    () => async () => {
      if (!credentials) {
        return
      }

      setError(null)
      try {
        const [notificationData, summaryData] = await Promise.all([
          fetchNotifications(credentials),
          fetchNotificationSummary(credentials),
        ])
        setNotifications(notificationData)
        setUnreadCount(summaryData.unreadCount)
      } catch {
        setError(t('notificationsLoadError'))
      }
    },
    [credentials, t],
  )

  useEffect(() => {
    void loadNotifications()
  }, [loadNotifications])

  const onMarkAsRead = async (notificationId: number) => {
    if (!credentials) {
      return
    }

    try {
      await markNotificationAsRead(credentials, notificationId)
      await loadNotifications()
    } catch {
      setError(t('notificationUpdateError'))
    }
  }

  const onMarkAllAsRead = async () => {
    if (!credentials) {
      return
    }

    try {
      await markAllNotificationsAsRead(credentials)
      await loadNotifications()
    } catch {
      setError(t('notificationUpdateError'))
    }
  }

  if (!isAuthenticated) {
    return <LoginRequired />
  }

  return (
    <section className="page-stack">
      <section className="wide-card">
        <div className="section-row">
          <div>
            <p className="eyebrow">Workflow Events</p>
            <h2>{t('notificationsTitle')}</h2>
            <p>{t('notificationsIntro')}</p>
          </div>
          <span className="role-chip">
            {t('unreadLabel')}: {unreadCount}
          </span>
        </div>
        {error ? <p className="error-text">{error}</p> : null}
        <div className="actions-row">
          <button type="button" onClick={() => void onMarkAllAsRead()} disabled={unreadCount === 0}>
            {t('markAllReadButton')}
          </button>
        </div>
      </section>

      <section className="wide-card">
        <div className="section-row">
          <h3>{t('notificationListTitle')}</h3>
          <span>{notifications.length}</span>
        </div>

        {notifications.length === 0 ? <p>{t('notificationsEmpty')}</p> : null}

        <div className="stack-sm">
          {notifications.map((notification) => (
            <article
              key={notification.id}
              className={`notification-card ${notification.read ? '' : 'unread'}`}
            >
              <div>
                <div className="section-row">
                  <strong>{notification.title}</strong>
                  <span className="role-chip">{notification.notificationType}</span>
                </div>
                <p>{notification.message}</p>
                {notification.submissionTitle ? <p className="muted-text">{notification.submissionTitle}</p> : null}
              </div>

              {!notification.read ? (
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => void onMarkAsRead(notification.id)}
                >
                  {t('markReadButton')}
                </button>
              ) : (
                <span className="muted-text">{t('readLabel')}</span>
              )}
            </article>
          ))}
        </div>
      </section>
    </section>
  )
}
