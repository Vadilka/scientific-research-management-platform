import { chromium } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:4173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080'
const prometheusUrl = process.env.PROMETHEUS_URL ?? 'http://localhost:9090'
const grafanaUrl = process.env.GRAFANA_URL ?? 'http://localhost:3000'
const screenshotDir = path.resolve('..', 'docs', 'assets', 'screenshots')
const timestamp = Date.now()

function basic(email, password) {
  return `Basic ${Buffer.from(`${email}:${password}`).toString('base64')}`
}

async function api(pathname, options = {}) {
  const response = await fetch(`${apiUrl}${pathname}`, {
    ...options,
    headers: {
      'content-type': 'application/json',
      ...(options.headers ?? {}),
    },
  })

  if (!response.ok) {
    throw new Error(`${options.method ?? 'GET'} ${pathname} failed with ${response.status}`)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

async function createSubmission(authorHeaders, authorEmail, categoryId, suffix, statusMode = 'submitted') {
  return api('/api/submissions', {
    method: 'POST',
    headers: authorHeaders,
    body: JSON.stringify({
      title: `${suffix} ${timestamp}`,
      abstractText:
        'This example article is generated for project documentation and demonstrates a realistic editorial workflow.',
      keywords: ['documentation', 'workflow', 'peer review'],
      correspondingAuthorEmail: authorEmail,
      categoryId,
      submitNow: statusMode !== 'draft',
      authors: [
        {
          fullName: 'Documentation Author',
          email: authorEmail,
          affiliation: 'Społeczna Akademia Nauk',
          authorOrder: 0,
          correspondingAuthor: true,
        },
        {
          fullName: 'Research Co Author',
          email: `coauthor.${timestamp}@san.local`,
          affiliation: 'Faculty of Computer Science',
          authorOrder: 1,
          correspondingAuthor: false,
        },
      ],
    }),
  })
}

async function prepareWorkflowData() {
  const authorEmail = `docs.author.${timestamp}@san.local`
  const password = 'password123'
  const authorHeaders = { Authorization: basic(authorEmail, password) }
  const adminHeaders = { Authorization: basic('admin@san.local', 'password') }
  const reviewerHeaders = { Authorization: basic('reviewer@san.local', 'password') }

  await api('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({
      fullName: 'Documentation Author',
      email: authorEmail,
      password,
    }),
  })

  const categories = await api('/api/categories', { headers: authorHeaders })
  const categoryId = categories[0].id

  const draftSubmission = await createSubmission(
    authorHeaders,
    authorEmail,
    categoryId,
    'Draft Article with Editable Metadata',
    'draft',
  )
  const publishedWorkflowSubmission = await createSubmission(
    authorHeaders,
    authorEmail,
    categoryId,
    'Published Documentation Workflow Article',
  )
  const pendingAssignmentSubmission = await createSubmission(
    authorHeaders,
    authorEmail,
    categoryId,
    'Article Waiting for Reviewer Assignment',
  )
  const pendingReviewSubmission = await createSubmission(
    authorHeaders,
    authorEmail,
    categoryId,
    'Article Waiting for Review Form',
  )
  const rejectedWorkflowSubmission = await createSubmission(
    authorHeaders,
    authorEmail,
    categoryId,
    'Rejected Documentation Workflow Article',
  )

  const publishedAssignment = await api('/api/review-assignments', {
    method: 'POST',
    headers: adminHeaders,
    body: JSON.stringify({
      submissionId: publishedWorkflowSubmission.id,
      reviewerEmail: 'reviewer@san.local',
      dueDate: null,
    }),
  })

  const pendingReviewAssignment = await api('/api/review-assignments', {
    method: 'POST',
    headers: adminHeaders,
    body: JSON.stringify({
      submissionId: pendingReviewSubmission.id,
      reviewerEmail: 'reviewer@san.local',
      dueDate: null,
    }),
  })

  const rejectedAssignment = await api('/api/review-assignments', {
    method: 'POST',
    headers: adminHeaders,
    body: JSON.stringify({
      submissionId: rejectedWorkflowSubmission.id,
      reviewerEmail: 'reviewer@san.local',
      dueDate: null,
    }),
  })

  await api('/api/reviews', {
    method: 'POST',
    headers: reviewerHeaders,
    body: JSON.stringify({
      assignmentId: publishedAssignment.id,
      score: 9,
      recommendation: 'ACCEPT',
      summaryComment: 'The article is clear and relevant.',
      detailedComment: 'The submission is suitable for acceptance in the demonstration workflow.',
    }),
  })

  await api('/api/reviews', {
    method: 'POST',
    headers: reviewerHeaders,
    body: JSON.stringify({
      assignmentId: rejectedAssignment.id,
      score: 3,
      recommendation: 'REJECT',
      summaryComment: 'The research problem is not described precisely enough.',
      detailedComment:
        'The example demonstrates that the workflow also stores negative editorial outcomes and reviewer comments.',
    }),
  })

  await api('/api/editorial-decisions', {
    method: 'POST',
    headers: adminHeaders,
    body: JSON.stringify({
      submissionId: publishedWorkflowSubmission.id,
      decisionType: 'ACCEPT',
      decisionNote: 'Accepted for documentation workflow.',
    }),
  })

  await api('/api/editorial-decisions', {
    method: 'POST',
    headers: adminHeaders,
    body: JSON.stringify({
      submissionId: publishedWorkflowSubmission.id,
      decisionType: 'PUBLISH',
      decisionNote: 'Published for documentation workflow.',
    }),
  })

  await api('/api/editorial-decisions', {
    method: 'POST',
    headers: adminHeaders,
    body: JSON.stringify({
      submissionId: rejectedWorkflowSubmission.id,
      decisionType: 'REJECT',
      decisionNote: 'Rejected because the article requires fundamental changes.',
    }),
  })

  return {
    author: { email: authorEmail, password },
    admin: { email: 'admin@san.local', password: 'password' },
    reviewer: { email: 'reviewer@san.local', password: 'password' },
    draftSubmissionId: draftSubmission.id,
    pendingAssignmentSubmissionId: pendingAssignmentSubmission.id,
    pendingReviewAssignmentId: pendingReviewAssignment.id,
    publishedWorkflowSubmissionId: publishedWorkflowSubmission.id,
  }
}

async function withSession(browser, credentials, route, fileName, preparePage) {
  const context = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
    deviceScaleFactor: 1,
  })

  await context.addInitScript((storedCredentials) => {
    localStorage.setItem('article-submission-auth', JSON.stringify(storedCredentials))
    localStorage.setItem('i18nextLng', 'pl')
  }, credentials)

  const page = await context.newPage()
  await page.goto(`${appUrl}${route}`)
  await page.waitForLoadState('networkidle')
  if (preparePage) {
    await preparePage(page)
    await page.waitForLoadState('networkidle')
  }
  await page.screenshot({
    path: path.join(screenshotDir, fileName),
    fullPage: true,
  })
  await context.close()
}

async function withoutSession(browser, route, fileName, preparePage) {
  const context = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
    deviceScaleFactor: 1,
  })
  await context.addInitScript(() => {
    localStorage.setItem('i18nextLng', 'pl')
  })

  const page = await context.newPage()
  await page.goto(`${appUrl}${route}`)
  await page.waitForLoadState('networkidle')
  if (preparePage) {
    await preparePage(page)
    await page.waitForLoadState('networkidle')
  }
  await page.screenshot({
    path: path.join(screenshotDir, fileName),
    fullPage: true,
  })
  await context.close()
}

async function capturePrometheus(browser) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 })
  await page.goto(`${prometheusUrl}/targets`)
  await page.waitForLoadState('networkidle')
  await page.screenshot({
    path: path.join(screenshotDir, '17-prometheus-targets.png'),
    fullPage: true,
  })
  await page.close()
}

async function captureGrafana(browser) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 })
  const dashboardUrl = `${grafanaUrl}/d/article-submission-overview/article-submission-overview?orgId=1&from=now-30m&to=now`

  await page.goto(`${grafanaUrl}/login`)
  await page.waitForLoadState('networkidle')

  if (await page.locator('input[name="user"]').isVisible().catch(() => false)) {
    await page.locator('input[name="user"]').fill('admin')
    await page.locator('input[name="password"]').fill('admin')
    await page.locator('button').filter({ hasText: 'Log in' }).click()
    await page.waitForLoadState('networkidle')
  }

  const skipButton = page.getByRole('button', { name: 'Skip' })
  if (await skipButton.isVisible().catch(() => false)) {
    await skipButton.click()
    await page.waitForLoadState('networkidle')
  }

  await page.goto(dashboardUrl)
  await page.waitForLoadState('networkidle')

  if (await page.locator('input[name="user"]').isVisible().catch(() => false)) {
    await page.locator('input[name="user"]').fill('admin')
    await page.locator('input[name="password"]').fill('admin')
    await page.locator('button').filter({ hasText: 'Log in' }).click()
    await page.waitForLoadState('networkidle')
    await page.goto(dashboardUrl)
    await page.waitForLoadState('networkidle')
  }

  await page.getByText('Total Submissions').waitFor({ state: 'visible', timeout: 20000 })
  await page.screenshot({
    path: path.join(screenshotDir, '18-grafana-dashboard.png'),
    fullPage: true,
  })
  await page.close()
}

async function main() {
  await mkdir(screenshotDir, { recursive: true })
  const users = await prepareWorkflowData()
  const browser = await chromium.launch()

  try {
    await withoutSession(browser, '/', '01-start-page-login.png', async (page) => {
      await page.locator('.auth-card input[type="email"]').fill('admin@san.local')
      await page.locator('.auth-card input[type="password"]').fill('password')
    })

    await withoutSession(browser, '/', '02-registration-form.png', async (page) => {
      await page.locator('.segmented-control button').nth(1).click()
      await page.locator('.auth-card input').nth(0).fill('New Portfolio User')
      await page.locator('.auth-card input').nth(1).fill(`new.user.${timestamp}@san.local`)
      await page.locator('.auth-card input').nth(2).fill('password123')
    })

    await withSession(browser, users.admin, '/', '03-home-admin-all-tabs.png')

    await withSession(browser, users.admin, '/submissions', '04-submission-create-form.png', async (page) => {
      const submissionForm = page
        .locator('section.wide-card')
        .filter({ hasText: 'Nowe zgłoszenie' })
        .locator('form')
      await submissionForm.locator('input[name="title"]').fill(`Wypełniony formularz artykułu ${timestamp}`)
      await submissionForm.locator('textarea[name="abstractText"]').fill(
        'Przykładowe streszczenie pokazuje, jakie informacje autor wprowadza przed wysłaniem artykułu do recenzji.',
      )
      await submissionForm.locator('input[name="keywordsText"]').fill('spring boot, react, peer review')
      await submissionForm.locator('input[name="correspondingAuthorEmail"]').fill(users.author.email)
      const categoryOptions = submissionForm.locator('select[name="categoryId"] option')
      await categoryOptions.nth(1).waitFor({ state: 'attached' })
      const firstCategoryValue = await categoryOptions.nth(1).getAttribute('value')
      await submissionForm.locator('select[name="categoryId"]').selectOption(firstCategoryValue)
      await submissionForm.locator('input[name="authors.0.fullName"]').fill('Documentation Author')
      await submissionForm.locator('input[name="authors.0.email"]').fill(users.author.email)
    })

    await withSession(browser, users.admin, '/submissions', '05-submission-file-upload.png', async (page) => {
      await page.locator('select[name="submissionId"]').selectOption(String(users.draftSubmissionId))
      await page.locator('select[name="fileType"]').selectOption('LATEX_SOURCE')
    })

    await withSession(browser, users.admin, '/submissions', '06-submission-filters.png', async (page) => {
      await page.locator('select[name="status"]').selectOption('IN_REVIEW')
      await page.locator('input[name="query"]').fill('Documentation')
    })

    await withSession(browser, users.admin, '/submissions', '07-submission-list-and-details.png', async (page) => {
      await page.getByText('Pokaż szczegóły').first().click()
      await page.waitForLoadState('networkidle')
    })

    await withSession(browser, users.admin, '/submissions', '08-draft-edit-mode.png', async (page) => {
      await page.getByText('Edytuj szkic').first().click()
      await page.waitForLoadState('networkidle')
    })

    await withSession(browser, users.admin, '/reviews', '09-review-assignment-form.png', async (page) => {
      await page.locator('select[name="submissionId"]').selectOption(String(users.pendingAssignmentSubmissionId))
      await page.locator('select[name="reviewerEmail"]').selectOption('reviewer@san.local')
      await page.locator('input[name="dueDate"]').fill('2026-05-20T12:00')
    })

    await withSession(browser, users.admin, '/reviews', '10-review-submit-form.png', async (page) => {
      await page.locator('select[name="assignmentId"]').selectOption(String(users.pendingReviewAssignmentId))
      await page.locator('input[name="score"]').fill('9')
      await page.locator('select[name="recommendation"]').selectOption('ACCEPT_WITH_CHANGES')
      await page.getByLabel('Komentarz skrócony').fill('Artykuł jest poprawny merytorycznie i wymaga drobnych zmian.')
      await page
        .getByLabel('Komentarz szczegółowy')
        .fill('Recenzja pokazuje sposób pracy recenzenta: ocena, rekomendacja oraz komentarz dla autora i redakcji.')
    })

    await withSession(browser, users.admin, '/reviews', '11-review-history.png', async (page) => {
      await page.getByText('Pokaż recenzje').first().click()
      await page.waitForLoadState('networkidle')
    })

    await withSession(browser, users.admin, '/published', '12-editorial-decision-form.png', async (page) => {
      await page.locator('select[name="submissionId"]').selectOption(String(users.publishedWorkflowSubmissionId))
      await page.locator('select[name="decisionType"]').selectOption('PUBLISH')
      await page.getByLabel('Notatka redakcyjna').fill('Publikacja zatwierdzona po pozytywnej recenzji.')
    })

    await withSession(browser, users.admin, '/published', '13-publication-queue-and-decisions.png', async (page) => {
      await page.getByText('Pokaż decyzje').first().click()
      await page.waitForLoadState('networkidle')
    })

    await withSession(browser, users.author, '/notifications', '14-notifications-author-events.png')
    await withSession(browser, users.admin, '/reports', '15-reports-and-exports.png')
    await withSession(browser, users.admin, '/admin', '16-admin-role-management.png')

    try {
      await capturePrometheus(browser)
    } catch (error) {
      console.warn(`Prometheus screenshot skipped: ${error.message}`)
    }

    try {
      await captureGrafana(browser)
    } catch (error) {
      console.warn(`Grafana screenshot skipped: ${error.message}`)
    }
  } finally {
    await browser.close()
  }

  console.log(`Screenshots saved to ${screenshotDir}`)
}

await main()
