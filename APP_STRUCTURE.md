.
├── app
│   ├── src
│   │   ├── androidTest
│   │   │   └── java
│   │   │       └── org
│   │   │           └── main
│   │   │               └── gitnex
│   │   │                   ├── activities
│   │   │                   │   └── MainActivityTest.java
│   │   │                   └── helpers
│   │   │                       ├── AppUtilTest.java
│   │   │                       └── ToastyTest.java
│   │   ├── main
│   │   │   ├── assets
│   │   │   │   └── fonts
│   │   │   │       ├── manroperegular.ttf
│   │   │   │       ├── roboto.ttf
│   │   │   │       └── sourcecodeproregular.ttf
│   │   │   ├── java
│   │   │   │   └── org
│   │   │   │       └── mian
│   │   │   │           └── gitnex
│   │   │   │               ├── actions
│   │   │   │               │   ├── ActionResult.java
│   │   │   │               │   ├── AssigneesActions.java
│   │   │   │               │   ├── CollaboratorActions.java
│   │   │   │               │   ├── IssueActions.java
│   │   │   │               │   ├── LabelsActions.java
│   │   │   │               │   ├── MilestoneActions.java
│   │   │   │               │   ├── PullRequestActions.java
│   │   │   │               │   ├── RepositoryActions.java
│   │   │   │               │   └── TeamActions.java
│   │   │   │               ├── activities
│   │   │   │               │   ├── AddCollaboratorToRepositoryActivity.java
│   │   │   │               │   ├── AddNewTeamMemberActivity.java
│   │   │   │               │   ├── AddNewTeamRepoActivity.java
│   │   │   │               │   ├── AdminCronTasksActivity.java
│   │   │   │               │   ├── AdminGetUsersActivity.java
│   │   │   │               │   ├── AdminUnadoptedReposActivity.java
│   │   │   │               │   ├── BaseActivity.java
│   │   │   │               │   ├── BiometricUnlock.java
│   │   │   │               │   ├── CodeEditorActivity.java
│   │   │   │               │   ├── CommitDetailActivity.java
│   │   │   │               │   ├── CommitsActivity.java
│   │   │   │               │   ├── CreateFileActivity.java
│   │   │   │               │   ├── CreateIssueActivity.java
│   │   │   │               │   ├── CreateLabelActivity.java
│   │   │   │               │   ├── CreateMilestoneActivity.java
│   │   │   │               │   ├── CreateNewUserActivity.java
│   │   │   │               │   ├── CreateNoteActivity.java
│   │   │   │               │   ├── CreateOrganizationActivity.java
│   │   │   │               │   ├── CreatePullRequestActivity.java
│   │   │   │               │   ├── CreateReleaseActivity.java
│   │   │   │               │   ├── CreateRepoActivity.java
│   │   │   │               │   ├── CreateTeamByOrgActivity.java
│   │   │   │               │   ├── DeepLinksActivity.java
│   │   │   │               │   ├── DiffActivity.java
│   │   │   │               │   ├── EditIssueActivity.java
│   │   │   │               │   ├── FileViewActivity.java
│   │   │   │               │   ├── IssueDetailActivity.java
│   │   │   │               │   ├── LoginActivity.java
│   │   │   │               │   ├── MainActivity.java
│   │   │   │               │   ├── MergePullRequestActivity.java
│   │   │   │               │   ├── OrganizationDetailActivity.java
│   │   │   │               │   ├── OrganizationTeamInfoActivity.java
│   │   │   │               │   ├── ProfileActivity.java
│   │   │   │               │   ├── RepoDetailActivity.java
│   │   │   │               │   ├── RepoForksActivity.java
│   │   │   │               │   ├── RepositoryActions.java
│   │   │   │               │   ├── RepositorySettingsActivity.java
│   │   │   │               │   ├── RepoStargazersActivity.java
│   │   │   │               │   ├── RepoWatchersActivity.java
│   │   │   │               │   └── WikiActivity.java
│   │   │   │               ├── adapters
│   │   │   │               │   ├── AccountSettingsEmailsAdapter.java
│   │   │   │               │   ├── ActivitiesAdapter.java
│   │   │   │               │   ├── AdminCronTasksAdapter.java
│   │   │   │               │   ├── AdminGetUsersAdapter.java
│   │   │   │               │   ├── AdminUnadoptedReposAdapter.java
│   │   │   │               │   ├── AssigneesListAdapter.java
│   │   │   │               │   ├── AttachmentsAdapter.java
│   │   │   │               │   ├── BranchAdapter.java
│   │   │   │               │   ├── CollaboratorsAdapter.java
│   │   │   │               │   ├── CollaboratorSearchAdapter.java
│   │   │   │               │   ├── CommitsAdapter.java
│   │   │   │               │   ├── CommitStatusesAdapter.java
│   │   │   │               │   ├── DependencyAdapter.java
│   │   │   │               │   ├── DiffAdapter.java
│   │   │   │               │   ├── DiffFilesAdapter.java
│   │   │   │               │   ├── ExploreIssuesAdapter.java
│   │   │   │               │   ├── ExploreRepositoriesAdapter.java
│   │   │   │               │   ├── FilesAdapter.java
│   │   │   │               │   ├── HeatmapAdapter.java
│   │   │   │               │   ├── HomeDashboardAdapter.java
│   │   │   │               │   ├── IssueCommentsAdapter.java
│   │   │   │               │   ├── IssuesAdapter.java
│   │   │   │               │   ├── LabelsAdapter.java
│   │   │   │               │   ├── LabelsListAdapter.java
│   │   │   │               │   ├── MilestonesAdapter.java
│   │   │   │               │   ├── MostVisitedReposAdapter.java
│   │   │   │               │   ├── NotesAdapter.java
│   │   │   │               │   ├── NotificationsAdapter.java
│   │   │   │               │   ├── OrganizationAddUserToTeamMemberAdapter.java
│   │   │   │               │   ├── OrganizationsListAdapter.java
│   │   │   │               │   ├── OrganizationTeamMembersPreviewAdapter.java
│   │   │   │               │   ├── OrganizationTeamRepositoriesAdapter.java
│   │   │   │               │   ├── OrganizationTeamsAdapter.java
│   │   │   │               │   ├── PullRequestsAdapter.java
│   │   │   │               │   ├── ReactionAuthorsAdapter.java
│   │   │   │               │   ├── ReleasesAdapter.java
│   │   │   │               │   ├── ReleasesDownloadsAdapter.java
│   │   │   │               │   ├── RepoForksAdapter.java
│   │   │   │               │   ├── ReposListAdapter.java
│   │   │   │               │   ├── SSHKeysAdapter.java
│   │   │   │               │   ├── TagsAdapter.java
│   │   │   │               │   ├── TrackedTimeAdapter.java
│   │   │   │               │   ├── UserAccountsAdapter.java
│   │   │   │               │   ├── UserAccountsNavAdapter.java
│   │   │   │               │   ├── UserGridAdapter.java
│   │   │   │               │   ├── UsersAdapter.java
│   │   │   │               │   └── WikiListAdapter.java
│   │   │   │               ├── clients
│   │   │   │               │   ├── GlideHttpClient.java
│   │   │   │               │   ├── GlideService.java
│   │   │   │               │   └── RetrofitClient.java
│   │   │   │               ├── core
│   │   │   │               │   ├── MainApplication.java
│   │   │   │               │   └── MainGrammarLocator.java
│   │   │   │               ├── database
│   │   │   │               │   ├── api
│   │   │   │               │   │   ├── AppSettingsApi.java
│   │   │   │               │   │   ├── BaseApi.java
│   │   │   │               │   │   ├── NotesApi.java
│   │   │   │               │   │   ├── RepositoriesApi.java
│   │   │   │               │   │   └── UserAccountsApi.java
│   │   │   │               │   ├── dao
│   │   │   │               │   │   ├── AppSettingsDao.java
│   │   │   │               │   │   ├── NotesDao.java
│   │   │   │               │   │   ├── RepositoriesDao.java
│   │   │   │               │   │   └── UserAccountsDao.java
│   │   │   │               │   ├── db
│   │   │   │               │   │   └── GitnexDatabase.java
│   │   │   │               │   └── models
│   │   │   │               │       ├── AppSettings.java
│   │   │   │               │       ├── DraftWithRepository.java
│   │   │   │               │       ├── Notes.java
│   │   │   │               │       ├── Repository.java
│   │   │   │               │       └── UserAccount.java
│   │   │   │               ├── fragments
│   │   │   │               │   ├── profile
│   │   │   │               │   │   ├── DetailFragment.java
│   │   │   │               │   │   ├── FollowersFragment.java
│   │   │   │               │   │   ├── FollowingFragment.java
│   │   │   │               │   │   ├── OrganizationsFragment.java
│   │   │   │               │   │   ├── RepositoriesFragment.java
│   │   │   │               │   │   └── StarredRepositoriesFragment.java
│   │   │   │               │   ├── AccountSettingsEmailsFragment.java
│   │   │   │               │   ├── AccountSettingsFragment.java
│   │   │   │               │   ├── ActivitiesFragment.java
│   │   │   │               │   ├── AdministrationFragment.java
│   │   │   │               │   ├── BottomSheetFileViewerFragment.java
│   │   │   │               │   ├── BottomSheetIssueDependenciesFragment.java
│   │   │   │               │   ├── BottomSheetIssuesFilterFragment.java
│   │   │   │               │   ├── BottomSheetMilestonesFilterFragment.java
│   │   │   │               │   ├── BottomSheetNotificationsFragment.java
│   │   │   │               │   ├── BottomSheetOrganizationFragment.java
│   │   │   │               │   ├── BottomSheetPullRequestFilterFragment.java
│   │   │   │               │   ├── BottomSheetReleasesTagsFragment.java
│   │   │   │               │   ├── BottomSheetRepoFragment.java
│   │   │   │               │   ├── BottomSheetSettingsAboutFragment.java
│   │   │   │               │   ├── BottomSheetSettingsAppearanceFragment.java
│   │   │   │               │   ├── BottomSheetSettingsBackupRestoreFragment.java
│   │   │   │               │   ├── BottomSheetSettingsCodeEditorFragment.java
│   │   │   │               │   ├── BottomSheetSettingsGeneralFragment.java
│   │   │   │               │   ├── BottomSheetSettingsNotificationsFragment.java
│   │   │   │               │   ├── BottomSheetSettingsSecurityFragment.java
│   │   │   │               │   ├── BottomSheetSingleIssueFragment.java
│   │   │   │               │   ├── BottomSheetTrackedTimeFragment.java
│   │   │   │               │   ├── BottomSheetUserProfileFragment.java
│   │   │   │               │   ├── BottomSheetWikiFragment.java
│   │   │   │               │   ├── CollaboratorsFragment.java
│   │   │   │               │   ├── CommitDetailFragment.java
│   │   │   │               │   ├── DiffFilesFragment.java
│   │   │   │               │   ├── DiffFragment.java
│   │   │   │               │   ├── ExploreFragment.java
│   │   │   │               │   ├── ExploreIssuesFragment.java
│   │   │   │               │   ├── ExplorePublicOrganizationsFragment.java
│   │   │   │               │   ├── ExploreRepositoriesFragment.java
│   │   │   │               │   ├── ExploreUsersFragment.java
│   │   │   │               │   ├── FilesFragment.java
│   │   │   │               │   ├── HomeDashboardFragment.java
│   │   │   │               │   ├── IssuesFragment.java
│   │   │   │               │   ├── LabelsFragment.java
│   │   │   │               │   ├── MilestonesFragment.java
│   │   │   │               │   ├── MostVisitedReposFragment.java
│   │   │   │               │   ├── MyIssuesFragment.java
│   │   │   │               │   ├── MyRepositoriesFragment.java
│   │   │   │               │   ├── NotesFragment.java
│   │   │   │               │   ├── NotificationsFragment.java
│   │   │   │               │   ├── OrganizationInfoFragment.java
│   │   │   │               │   ├── OrganizationLabelsFragment.java
│   │   │   │               │   ├── OrganizationMembersFragment.java
│   │   │   │               │   ├── OrganizationRepositoriesFragment.java
│   │   │   │               │   ├── OrganizationsFragment.java
│   │   │   │               │   ├── OrganizationTeamInfoMembersFragment.java
│   │   │   │               │   ├── OrganizationTeamInfoPermissionsFragment.java
│   │   │   │               │   ├── OrganizationTeamInfoReposFragment.java
│   │   │   │               │   ├── OrganizationTeamsFragment.java
│   │   │   │               │   ├── PullRequestChangesFragment.java
│   │   │   │               │   ├── PullRequestCommitsFragment.java
│   │   │   │               │   ├── PullRequestsFragment.java
│   │   │   │               │   ├── ReleasesFragment.java
│   │   │   │               │   ├── RepoInfoFragment.java
│   │   │   │               │   ├── RepositoriesFragment.java
│   │   │   │               │   ├── SettingsFragment.java
│   │   │   │               │   ├── SSHKeysFragment.java
│   │   │   │               │   ├── StarredRepositoriesFragment.java
│   │   │   │               │   ├── WatchedRepositoriesFragment.java
│   │   │   │               │   └── WikiFragment.java
│   │   │   │               ├── helpers
│   │   │   │               │   ├── attachments
│   │   │   │               │   │   ├── AttachmentsModel.java
│   │   │   │               │   │   └── AttachmentUtils.java
│   │   │   │               │   ├── codeeditor
│   │   │   │               │   │   ├── languages
│   │   │   │               │   │   │   ├── BashLanguage.java
│   │   │   │               │   │   │   ├── CLanguage.java
│   │   │   │               │   │   │   ├── CppLanguage.java
│   │   │   │               │   │   │   ├── DLanguage.java
│   │   │   │               │   │   │   ├── GoLanguage.java
│   │   │   │               │   │   │   ├── HtmlLanguage.java
│   │   │   │               │   │   │   ├── JavaLanguage.java
│   │   │   │               │   │   │   ├── JavaScriptLanguage.java
│   │   │   │               │   │   │   ├── JsonLanguage.java
│   │   │   │               │   │   │   ├── LanguageElement.java
│   │   │   │               │   │   │   ├── Language.java
│   │   │   │               │   │   │   ├── LispLanguage.java
│   │   │   │               │   │   │   ├── PhpLanguage.java
│   │   │   │               │   │   │   ├── PythonLanguage.java
│   │   │   │               │   │   │   ├── TypeScriptLanguage.java
│   │   │   │               │   │   │   ├── UnknownLanguage.java
│   │   │   │               │   │   │   └── XmlLanguage.java
│   │   │   │               │   │   ├── markwon
│   │   │   │               │   │   │   ├── MarkwonHighlighter.java
│   │   │   │               │   │   │   └── SyntaxHighlighter.java
│   │   │   │               │   │   ├── theme
│   │   │   │               │   │   │   ├── BlueMoonDarkTheme.java
│   │   │   │               │   │   │   ├── BlueMoonTheme.java
│   │   │   │               │   │   │   ├── FiveColorsDarkTheme.java
│   │   │   │               │   │   │   ├── FiveColorsTheme.java
│   │   │   │               │   │   │   └── Theme.java
│   │   │   │               │   │   ├── CustomCodeViewAdapter.java
│   │   │   │               │   │   └── SourcePositionListener.java
│   │   │   │               │   ├── contexts
│   │   │   │               │   │   ├── AccountContext.java
│   │   │   │               │   │   ├── IssueContext.java
│   │   │   │               │   │   └── RepositoryContext.java
│   │   │   │               │   ├── languagestatistics
│   │   │   │               │   │   ├── LanguageColor.java
│   │   │   │               │   │   ├── LanguageStatisticsHelper.java
│   │   │   │               │   │   ├── LanguageStatisticsView.java
│   │   │   │               │   │   └── SeekbarItem.java
│   │   │   │               │   ├── markdown
│   │   │   │               │   │   └── AlertPlugin.java
│   │   │   │               │   ├── ssl
│   │   │   │               │   │   ├── MemorizingTrustManager.java
│   │   │   │               │   │   └── MTMDecision.java
│   │   │   │               │   ├── AlertDialogs.java
│   │   │   │               │   ├── AppDatabaseSettings.java
│   │   │   │               │   ├── AppUtil.java
│   │   │   │               │   ├── BackupUtil.java
│   │   │   │               │   ├── ChangeLog.java
│   │   │   │               │   ├── ClickListener.java
│   │   │   │               │   ├── ColorInverter.java
│   │   │   │               │   ├── Constants.java
│   │   │   │               │   ├── FileContentSearcher.java
│   │   │   │               │   ├── FileDiffView.java
│   │   │   │               │   ├── FileIcon.java
│   │   │   │               │   ├── FilesData.java
│   │   │   │               │   ├── FontsOverride.java
│   │   │   │               │   ├── Images.java
│   │   │   │               │   ├── LabelWidthCalculator.java
│   │   │   │               │   ├── Markdown.java
│   │   │   │               │   ├── MentionHelper.java
│   │   │   │               │   ├── MergePullRequestSpinner.java
│   │   │   │               │   ├── NetworkStatusObserver.java
│   │   │   │               │   ├── ParseDiff.java
│   │   │   │               │   ├── Path.java
│   │   │   │               │   ├── PathsHelper.java
│   │   │   │               │   ├── RecyclerViewEmptySupport.java
│   │   │   │               │   ├── SimpleCallback.java
│   │   │   │               │   ├── SnackBar.java
│   │   │   │               │   ├── TimeHelper.java
│   │   │   │               │   ├── TinyDB.java
│   │   │   │               │   ├── Toasty.java
│   │   │   │               │   ├── UrlHelper.java
│   │   │   │               │   ├── Version.java
│   │   │   │               │   └── ViewPager2Transformers.java
│   │   │   │               ├── notifications
│   │   │   │               │   ├── Notifications.java
│   │   │   │               │   └── NotificationsWorker.java
│   │   │   │               ├── structs
│   │   │   │               │   ├── BottomSheetListener.java
│   │   │   │               │   ├── FragmentRefreshListener.java
│   │   │   │               │   └── Protocol.java
│   │   │   │               ├── viewmodels
│   │   │   │               │   ├── AccountSettingsEmailsViewModel.java
│   │   │   │               │   ├── AccountSettingsSSHKeysViewModel.java
│   │   │   │               │   ├── ActionsVariablesViewModel.java
│   │   │   │               │   ├── ActivitiesViewModel.java
│   │   │   │               │   ├── AdminCronTasksViewModel.java
│   │   │   │               │   ├── AdminGetUsersViewModel.java
│   │   │   │               │   ├── AdminUnadoptedReposViewModel.java
│   │   │   │               │   ├── CollaboratorsViewModel.java
│   │   │   │               │   ├── FilesViewModel.java
│   │   │   │               │   ├── IssueCommentsViewModel.java
│   │   │   │               │   ├── IssuesViewModel.java
│   │   │   │               │   ├── LabelsViewModel.java
│   │   │   │               │   ├── MembersByOrgTeamViewModel.java
│   │   │   │               │   ├── MembersByOrgViewModel.java
│   │   │   │               │   ├── MilestonesViewModel.java
│   │   │   │               │   ├── OrganizationsViewModel.java
│   │   │   │               │   ├── ReleasesViewModel.java
│   │   │   │               │   ├── RepositoriesViewModel.java
│   │   │   │               │   ├── RepositoryForksViewModel.java
│   │   │   │               │   ├── RepoStargazersViewModel.java
│   │   │   │               │   ├── RepoWatchersViewModel.java
│   │   │   │               │   ├── TeamsByOrgViewModel.java
│   │   │   │               │   └── WikiViewModel.java
│   │   │   │               └── views
│   │   │   │                   ├── ReactionList.java
│   │   │   │                   ├── ReactionSpinner.java
│   │   │   │                   ├── SyntaxHighlightedArea.java
│   │   │   │                   ├── ZoomableSyntaxHighlightedArea.java
│   │   │   │                   └── ZoomableTextView.java
│   │   │   ├── res
│   │   │   │   ├── drawable
│   │   │   │   │   ├── app_logo_background.xml
│   │   │   │   │   ├── app_logo_monochrome.xml
│   │   │   │   │   ├── app_logo.xml
│   │   │   │   │   ├── bottom_sheet_handle.xml
│   │   │   │   │   ├── gitnex.png
│   │   │   │   │   ├── ic_account_settings.xml
│   │   │   │   │   ├── ic_actions.xml
│   │   │   │   │   ├── ic_activities.xml
│   │   │   │   │   ├── ic_add.xml
│   │   │   │   │   ├── ic_am.xml
│   │   │   │   │   ├── ic_android.xml
│   │   │   │   │   ├── ic_apache.xml
│   │   │   │   │   ├── ic_appearance.xml
│   │   │   │   │   ├── ic_arrow_back.xml
│   │   │   │   │   ├── ic_arrow_down.xml
│   │   │   │   │   ├── ic_arrow_right.xml
│   │   │   │   │   ├── ic_arrow_up.xml
│   │   │   │   │   ├── ic_attachment.xml
│   │   │   │   │   ├── ic_audio.xml
│   │   │   │   │   ├── ic_bash.xml
│   │   │   │   │   ├── ic_bat.xml
│   │   │   │   │   ├── ic_bookmark.xml
│   │   │   │   │   ├── ic_branch.xml
│   │   │   │   │   ├── ic_browser.xml
│   │   │   │   │   ├── ic_bsh.xml
│   │   │   │   │   ├── ic_bug_report.xml
│   │   │   │   │   ├── ic_calendar.xml
│   │   │   │   │   ├── ic_caution.xml
│   │   │   │   │   ├── ic_cc.xml
│   │   │   │   │   ├── ic_check.xml
│   │   │   │   │   ├── ic_chevron_down.xml
│   │   │   │   │   ├── ic_chevron_left.xml
│   │   │   │   │   ├── ic_chevron_right.xml
│   │   │   │   │   ├── ic_chevron_up.xml
│   │   │   │   │   ├── ic_clj.xml
│   │   │   │   │   ├── ic_clock.xml
│   │   │   │   │   ├── ic_close.xml
│   │   │   │   │   ├── ic_cmake.xml
│   │   │   │   │   ├── ic_code_v2.xml
│   │   │   │   │   ├── ic_code.xml
│   │   │   │   │   ├── ic_coffee.xml
│   │   │   │   │   ├── ic_comment.xml
│   │   │   │   │   ├── ic_commit.xml
│   │   │   │   │   ├── ic_conf.xml
│   │   │   │   │   ├── ic_copy.xml
│   │   │   │   │   ├── ic_cpp.xml
│   │   │   │   │   ├── ic_css.xml
│   │   │   │   │   ├── ic_csv.xml
│   │   │   │   │   ├── ic_cs.xml
│   │   │   │   │   ├── ic_cvs.xml
│   │   │   │   │   ├── ic_c.xml
│   │   │   │   │   ├── ic_cxx.xml
│   │   │   │   │   ├── ic_cyc.xml
│   │   │   │   │   ├── ic_dart.xml
│   │   │   │   │   ├── ic_dashboard.xml
│   │   │   │   │   ├── ic_delete.xml
│   │   │   │   │   ├── ic_dependencies.xml
│   │   │   │   │   ├── ic_dependency.xml
│   │   │   │   │   ├── ic_diff.xml
│   │   │   │   │   ├── ic_directory.xml
│   │   │   │   │   ├── ic_dist.xml
│   │   │   │   │   ├── ic_document.xml
│   │   │   │   │   ├── ic_done.xml
│   │   │   │   │   ├── ic_dot_fill.xml
│   │   │   │   │   ├── ic_dotted_menu_horizontal.xml
│   │   │   │   │   ├── ic_dotted_menu.xml
│   │   │   │   │   ├── ic_download.xml
│   │   │   │   │   ├── ic_drafts.xml
│   │   │   │   │   ├── ic_draft.xml
│   │   │   │   │   ├── ic_d.xml
│   │   │   │   │   ├── ic_editorconfig.xml
│   │   │   │   │   ├── ic_edit.xml
│   │   │   │   │   ├── ic_el.xml
│   │   │   │   │   ├── ic_email.xml
│   │   │   │   │   ├── ic_erl.xml
│   │   │   │   │   ├── ic_executable.xml
│   │   │   │   │   ├── ic_export.xml
│   │   │   │   │   ├── ic_feedback.xml
│   │   │   │   │   ├── ic_file_code.xml
│   │   │   │   │   ├── ic_file_description.xml
│   │   │   │   │   ├── ic_file_download.xml
│   │   │   │   │   ├── ic_file_lock.xml
│   │   │   │   │   ├── ic_file_next.xml
│   │   │   │   │   ├── ic_file.xml
│   │   │   │   │   ├── ic_filter_closed.xml
│   │   │   │   │   ├── ic_filter.xml
│   │   │   │   │   ├── ic_flag.xml
│   │   │   │   │   ├── ic_flame.xml
│   │   │   │   │   ├── ic_font.xml
│   │   │   │   │   ├── ic_fork.xml
│   │   │   │   │   ├── ic_git.xml
│   │   │   │   │   ├── ic_go.xml
│   │   │   │   │   ├── ic_gradle.xml
│   │   │   │   │   ├── ic_history.xml
│   │   │   │   │   ├── ic_home.xml
│   │   │   │   │   ├── ic_hs.xml
│   │   │   │   │   ├── ic_html.xml
│   │   │   │   │   ├── ic_htm.xml
│   │   │   │   │   ├── ic_h.xml
│   │   │   │   │   ├── ic_hx.xml
│   │   │   │   │   ├── ic_image.xml
│   │   │   │   │   ├── ic_important.xml
│   │   │   │   │   ├── ic_info.xml
│   │   │   │   │   ├── ic_in.xml
│   │   │   │   │   ├── ic_issue_closed.xml
│   │   │   │   │   ├── ic_issue.xml
│   │   │   │   │   ├── ic_javascript.xml
│   │   │   │   │   ├── ic_java.xml
│   │   │   │   │   ├── ic_jenkins.xml
│   │   │   │   │   ├── ic_json.xml
│   │   │   │   │   ├── ic_js.xml
│   │   │   │   │   ├── ic_kanban.xml
│   │   │   │   │   ├── ic_key.xml
│   │   │   │   │   ├── ic_kotlin.xml
│   │   │   │   │   ├── ic_label.xml
│   │   │   │   │   ├── ic_language.xml
│   │   │   │   │   ├── ic_latex.xml
│   │   │   │   │   ├── ic_license.xml
│   │   │   │   │   ├── ic_like.xml
│   │   │   │   │   ├── ic_link.xml
│   │   │   │   │   ├── ic_lisp.xml
│   │   │   │   │   ├── ic_loader.xml
│   │   │   │   │   ├── ic_location.xml
│   │   │   │   │   ├── ic_lock.xml
│   │   │   │   │   ├── ic_lsp.xml
│   │   │   │   │   ├── ic_lua.xml
│   │   │   │   │   ├── ic_markdown.xml
│   │   │   │   │   ├── ic_matlab.xml
│   │   │   │   │   ├── ic_md.xml
│   │   │   │   │   ├── ic_menu.xml
│   │   │   │   │   ├── ic_migrate.xml
│   │   │   │   │   ├── ic_milestone.xml
│   │   │   │   │   ├── ic_m.xml
│   │   │   │   │   ├── ic_node_js.xml
│   │   │   │   │   ├── ic_notes.xml
│   │   │   │   │   ├── ic_notifications.xml
│   │   │   │   │   ├── ic_organization.xml
│   │   │   │   │   ├── ic_otp.xml
│   │   │   │   │   ├── ic_pascal.xml
│   │   │   │   │   ├── ic_patreon.xml
│   │   │   │   │   ├── ic_people.xml
│   │   │   │   │   ├── ic_perl.xml
│   │   │   │   │   ├── ic_person_add.xml
│   │   │   │   │   ├── ic_person_remove.xml
│   │   │   │   │   ├── ic_person.xml
│   │   │   │   │   ├── ic_php.xml
│   │   │   │   │   ├── ic_pin.xml
│   │   │   │   │   ├── ic_play.xml
│   │   │   │   │   ├── ic_pl.xml
│   │   │   │   │   ├── ic_pm.xml
│   │   │   │   │   ├── ic_prolog.xml
│   │   │   │   │   ├── ic_proto.xml
│   │   │   │   │   ├── ic_pull_request.xml
│   │   │   │   │   ├── ic_python.xml
│   │   │   │   │   ├── ic_py.xml
│   │   │   │   │   ├── ic_question.xml
│   │   │   │   │   ├── ic_rb.xml
│   │   │   │   │   ├── ic_rc.xml
│   │   │   │   │   ├── ic_refresh.xml
│   │   │   │   │   ├── ic_release.xml
│   │   │   │   │   ├── ic_remove.xml
│   │   │   │   │   ├── ic_reply.xml
│   │   │   │   │   ├── ic_repo.xml
│   │   │   │   │   ├── ic_rkt.xml
│   │   │   │   │   ├── ic_rs.xml
│   │   │   │   │   ├── ic_ruby.xml
│   │   │   │   │   ├── ic_rust.xml
│   │   │   │   │   ├── ic_r.xml
│   │   │   │   │   ├── ic_save.xml
│   │   │   │   │   ├── ic_scala.xml
│   │   │   │   │   ├── ic_search.xml
│   │   │   │   │   ├── ic_security.xml
│   │   │   │   │   ├── ic_send.xml
│   │   │   │   │   ├── ic_settings.xml
│   │   │   │   │   ├── ic_share.xml
│   │   │   │   │   ├── ic_sh.xml
│   │   │   │   │   ├── ic_snippet.xml
│   │   │   │   │   ├── ic_spec.xml
│   │   │   │   │   ├── ic_sql.xml
│   │   │   │   │   ├── ic_ss.xml
│   │   │   │   │   ├── ic_star_unfilled.xml
│   │   │   │   │   ├── ic_star.xml
│   │   │   │   │   ├── ic_stopwatch.xml
│   │   │   │   │   ├── ic_submodule.xml
│   │   │   │   │   ├── ic_symlink.xml
│   │   │   │   │   ├── ic_tag.xml
│   │   │   │   │   ├── ic_tasks.xml
│   │   │   │   │   ├── ic_tcl.xml
│   │   │   │   │   ├── ic_text.xml
│   │   │   │   │   ├── ic_tip.xml
│   │   │   │   │   ├── ic_toml.xml
│   │   │   │   │   ├── ic_tool.xml
│   │   │   │   │   ├── ic_trending.xml
│   │   │   │   │   ├── ic_ts.xml
│   │   │   │   │   ├── ic_twig.xml
│   │   │   │   │   ├── ic_unlock.xml
│   │   │   │   │   ├── ic_unpin.xml
│   │   │   │   │   ├── ic_unwatch.xml
│   │   │   │   │   ├── ic_vbs.xml
│   │   │   │   │   ├── ic_vb.xml
│   │   │   │   │   ├── ic_verified_user.xml
│   │   │   │   │   ├── ic_vhd.xml
│   │   │   │   │   ├── ic_video.xml
│   │   │   │   │   ├── ic_volt.xml
│   │   │   │   │   ├── ic_vue.xml
│   │   │   │   │   ├── ic_warning.xml
│   │   │   │   │   ├── ic_watchers.xml
│   │   │   │   │   ├── ic_wiki.xml
│   │   │   │   │   ├── ic_xml.xml
│   │   │   │   │   ├── ic_yaml.xml
│   │   │   │   │   ├── loader_animated.xml
│   │   │   │   │   ├── progress_bar.xml
│   │   │   │   │   ├── shape_archived.xml
│   │   │   │   │   ├── shape_badge_background.xml
│   │   │   │   │   ├── shape_beta_badge.xml
│   │   │   │   │   ├── shape_bottom_sheet_top_corners.xml
│   │   │   │   │   ├── shape_circle_red.xml
│   │   │   │   │   ├── shape_circle.xml
│   │   │   │   │   ├── shape_draft_release.xml
│   │   │   │   │   ├── shape_full_circle.xml
│   │   │   │   │   ├── shape_inputs.xml
│   │   │   │   │   ├── shape_pre_release.xml
│   │   │   │   │   ├── shape_round_corners.xml
│   │   │   │   │   └── shape_stable_release.xml
│   │   │   │   ├── drawable-anydpi
│   │   │   │   ├── drawable-anydpi-v24
│   │   │   │   │   └── gitnex_transparent.xml
│   │   │   │   ├── drawable-hdpi
│   │   │   │   │   └── gitnex_transparent.png
│   │   │   │   ├── drawable-mdpi
│   │   │   │   │   └── gitnex_transparent.png
│   │   │   │   ├── drawable-xhdpi
│   │   │   │   │   └── gitnex_transparent.png
│   │   │   │   ├── drawable-xxhdpi
│   │   │   │   │   └── gitnex_transparent.png
│   │   │   │   ├── drawable-xxxhdpi
│   │   │   │   │   └── gitnex_transparent.png
│   │   │   │   ├── layout
│   │   │   │   │   ├── activity_add_collaborator_to_repository.xml
│   │   │   │   │   ├── activity_add_new_team_member.xml
│   │   │   │   │   ├── activity_admin_cron_tasks.xml
│   │   │   │   │   ├── activity_admin_get_users.xml
│   │   │   │   │   ├── activity_code_editor.xml
│   │   │   │   │   ├── activity_commit_details.xml
│   │   │   │   │   ├── activity_commits.xml
│   │   │   │   │   ├── activity_create_file.xml
│   │   │   │   │   ├── activity_create_issue.xml
│   │   │   │   │   ├── activity_create_label.xml
│   │   │   │   │   ├── activity_create_milestone.xml
│   │   │   │   │   ├── activity_create_new_user.xml
│   │   │   │   │   ├── activity_create_note.xml
│   │   │   │   │   ├── activity_create_organization.xml
│   │   │   │   │   ├── activity_create_pr.xml
│   │   │   │   │   ├── activity_create_release.xml
│   │   │   │   │   ├── activity_create_repo.xml
│   │   │   │   │   ├── activity_create_team_by_org.xml
│   │   │   │   │   ├── activity_deeplinks.xml
│   │   │   │   │   ├── activity_diff.xml
│   │   │   │   │   ├── activity_edit_issue.xml
│   │   │   │   │   ├── activity_file_view.xml
│   │   │   │   │   ├── activity_issue_detail.xml
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_merge_pull_request.xml
│   │   │   │   │   ├── activity_org_detail.xml
│   │   │   │   │   ├── activity_org_team_info.xml
│   │   │   │   │   ├── activity_profile.xml
│   │   │   │   │   ├── activity_repo_detail.xml
│   │   │   │   │   ├── activity_repo_forks.xml
│   │   │   │   │   ├── activity_repository_actions.xml
│   │   │   │   │   ├── activity_repository_settings.xml
│   │   │   │   │   ├── activity_repo_stargazers.xml
│   │   │   │   │   ├── activity_repo_watchers.xml
│   │   │   │   │   ├── activity_unlock.xml
│   │   │   │   │   ├── activity_wiki.xml
│   │   │   │   │   ├── add_new_team_repository.xml
│   │   │   │   │   ├── badge_beta.xml
│   │   │   │   │   ├── badge_issue.xml
│   │   │   │   │   ├── badge_notification.xml
│   │   │   │   │   ├── badge_pull.xml
│   │   │   │   │   ├── badge_release.xml
│   │   │   │   │   ├── bottom_sheet_attachments.xml
│   │   │   │   │   ├── bottom_sheet_create_action_variable.xml
│   │   │   │   │   ├── bottom_sheet_explore_filters.xml
│   │   │   │   │   ├── bottom_sheet_file_viewer.xml
│   │   │   │   │   ├── bottom_sheet_issue_comments.xml
│   │   │   │   │   ├── bottom_sheet_issue_dependencies.xml
│   │   │   │   │   ├── bottom_sheet_issues_filter.xml
│   │   │   │   │   ├── bottom_sheet_labels_in_list.xml
│   │   │   │   │   ├── bottom_sheet_milestones_filter.xml
│   │   │   │   │   ├── bottom_sheet_milestones_in_list.xml
│   │   │   │   │   ├── bottom_sheet_my_issues_filter.xml
│   │   │   │   │   ├── bottom_sheet_notifications.xml
│   │   │   │   │   ├── bottom_sheet_organization.xml
│   │   │   │   │   ├── bottom_sheet_pull_request_filter.xml
│   │   │   │   │   ├── bottom_sheet_release_in_list.xml
│   │   │   │   │   ├── bottom_sheet_releases_tags.xml
│   │   │   │   │   ├── bottom_sheet_repositories_sort.xml
│   │   │   │   │   ├── bottom_sheet_repo.xml
│   │   │   │   │   ├── bottom_sheet_settings_about.xml
│   │   │   │   │   ├── bottom_sheet_settings_appearance.xml
│   │   │   │   │   ├── bottom_sheet_settings_backup_restore.xml
│   │   │   │   │   ├── bottom_sheet_settings_code_editor.xml
│   │   │   │   │   ├── bottom_sheet_settings_general.xml
│   │   │   │   │   ├── bottom_sheet_settings_notifications.xml
│   │   │   │   │   ├── bottom_sheet_settings_security.xml
│   │   │   │   │   ├── bottom_sheet_single_issue.xml
│   │   │   │   │   ├── bottom_sheet_tag_in_list.xml
│   │   │   │   │   ├── bottom_sheet_tracked_time.xml
│   │   │   │   │   ├── bottom_sheet_user_profile.xml
│   │   │   │   │   ├── bottom_sheet_wiki_in_list.xml
│   │   │   │   │   ├── chip_item.xml
│   │   │   │   │   ├── custom_account_settings_add_new_email.xml
│   │   │   │   │   ├── custom_account_settings_add_ssh_key.xml
│   │   │   │   │   ├── custom_assignees_list.xml
│   │   │   │   │   ├── custom_assignees_selection_dialog.xml
│   │   │   │   │   ├── custom_branches_dialog.xml
│   │   │   │   │   ├── custom_create_branch_dialog.xml
│   │   │   │   │   ├── custom_edit_avatar_dialog.xml
│   │   │   │   │   ├── custom_edit_profile.xml
│   │   │   │   │   ├── custom_filter_issues_by_labels.xml
│   │   │   │   │   ├── custom_image_view_dialog.xml
│   │   │   │   │   ├── custom_insert_note.xml
│   │   │   │   │   ├── custom_labels_list.xml
│   │   │   │   │   ├── custom_labels_selection_dialog.xml
│   │   │   │   │   ├── custom_markdown_adapter.xml
│   │   │   │   │   ├── custom_markdown_alert_block.xml
│   │   │   │   │   ├── custom_markdown_code_block.xml
│   │   │   │   │   ├── custom_markdown_table.xml
│   │   │   │   │   ├── custom_pr_info_dialog.xml
│   │   │   │   │   ├── custom_progress_loader.xml
│   │   │   │   │   ├── custom_pr_update_strategy_dialog.xml
│   │   │   │   │   ├── custom_repository_delete_dialog.xml
│   │   │   │   │   ├── custom_repository_edit_properties_dialog.xml
│   │   │   │   │   ├── custom_repository_transfer_dialog.xml
│   │   │   │   │   ├── custom_toast_error.xml
│   │   │   │   │   ├── custom_toast_info.xml
│   │   │   │   │   ├── custom_toast_success.xml
│   │   │   │   │   ├── custom_toast_warning.xml
│   │   │   │   │   ├── custom_user_accounts_dialog.xml
│   │   │   │   │   ├── fragment_account_settings_emails.xml
│   │   │   │   │   ├── fragment_account_settings_ssh_keys.xml
│   │   │   │   │   ├── fragment_account_settings.xml
│   │   │   │   │   ├── fragment_activities.xml
│   │   │   │   │   ├── fragment_administration.xml
│   │   │   │   │   ├── fragment_collaborators.xml
│   │   │   │   │   ├── fragment_commit_details.xml
│   │   │   │   │   ├── fragment_diff_files.xml
│   │   │   │   │   ├── fragment_diff.xml
│   │   │   │   │   ├── fragment_explore_repo.xml
│   │   │   │   │   ├── fragment_explore_users.xml
│   │   │   │   │   ├── fragment_explore.xml
│   │   │   │   │   ├── fragment_files.xml
│   │   │   │   │   ├── fragment_home_dashboard.xml
│   │   │   │   │   ├── fragment_issues.xml
│   │   │   │   │   ├── fragment_labels.xml
│   │   │   │   │   ├── fragment_milestones.xml
│   │   │   │   │   ├── fragment_most_visited.xml
│   │   │   │   │   ├── fragment_notes.xml
│   │   │   │   │   ├── fragment_notifications.xml
│   │   │   │   │   ├── fragment_organization_info.xml
│   │   │   │   │   ├── fragment_organization_members.xml
│   │   │   │   │   ├── fragment_organizations.xml
│   │   │   │   │   ├── fragment_organization_team_info_members.xml
│   │   │   │   │   ├── fragment_organization_team_info_permissions.xml
│   │   │   │   │   ├── fragment_organization_teams.xml
│   │   │   │   │   ├── fragment_pr_changes.xml
│   │   │   │   │   ├── fragment_profile_detail.xml
│   │   │   │   │   ├── fragment_profile_followers_following.xml
│   │   │   │   │   ├── fragment_pull_requests.xml
│   │   │   │   │   ├── fragment_releases.xml
│   │   │   │   │   ├── fragment_repo_info.xml
│   │   │   │   │   ├── fragment_repositories.xml
│   │   │   │   │   ├── fragment_search_issues.xml
│   │   │   │   │   ├── fragment_settings.xml
│   │   │   │   │   ├── fragment_wiki.xml
│   │   │   │   │   ├── layout_cron_task_info.xml
│   │   │   │   │   ├── layout_deprecation_dialog.xml
│   │   │   │   │   ├── layout_reaction_badge.xml
│   │   │   │   │   ├── layout_reaction_button.xml
│   │   │   │   │   ├── layout_repo_language_statistics.xml
│   │   │   │   │   ├── layout_repo_more_info.xml
│   │   │   │   │   ├── layout_tab_text.xml
│   │   │   │   │   ├── list_account_settings_emails.xml
│   │   │   │   │   ├── list_account_settings_ssh_keys.xml
│   │   │   │   │   ├── list_action_runners.xml
│   │   │   │   │   ├── list_action_variables.xml
│   │   │   │   │   ├── list_action_workflows.xml
│   │   │   │   │   ├── list_activities.xml
│   │   │   │   │   ├── list_admin_cron_tasks.xml
│   │   │   │   │   ├── list_admin_unadopted_repos.xml
│   │   │   │   │   ├── list_admin_users.xml
│   │   │   │   │   ├── list_attachments.xml
│   │   │   │   │   ├── list_branches.xml
│   │   │   │   │   ├── list_collaborators_search.xml
│   │   │   │   │   ├── list_collaborators.xml
│   │   │   │   │   ├── list_commit_status.xml
│   │   │   │   │   ├── list_commits.xml
│   │   │   │   │   ├── list_diff_files.xml
│   │   │   │   │   ├── list_files.xml
│   │   │   │   │   ├── list_filter_issues_by_labels.xml
│   │   │   │   │   ├── list_home_dashboard_item.xml
│   │   │   │   │   ├── list_home_dashboard_subitem.xml
│   │   │   │   │   ├── list_issue_comments.xml
│   │   │   │   │   ├── list_issue_dependency.xml
│   │   │   │   │   ├── list_issues_pinned.xml
│   │   │   │   │   ├── list_issues.xml
│   │   │   │   │   ├── list_items_autocomplete.xml
│   │   │   │   │   ├── list_item_suggestion.xml
│   │   │   │   │   ├── list_labels.xml
│   │   │   │   │   ├── list_milestones.xml
│   │   │   │   │   ├── list_most_visited_repos.xml
│   │   │   │   │   ├── list_notes.xml
│   │   │   │   │   ├── list_notifications.xml
│   │   │   │   │   ├── list_organization_members_preview.xml
│   │   │   │   │   ├── list_organizations.xml
│   │   │   │   │   ├── list_organization_teams.xml
│   │   │   │   │   ├── list_pr.xml
│   │   │   │   │   ├── list_reaction_authors.xml
│   │   │   │   │   ├── list_releases_downloads.xml
│   │   │   │   │   ├── list_releases.xml
│   │   │   │   │   ├── list_repositories.xml
│   │   │   │   │   ├── list_spinner_items.xml
│   │   │   │   │   ├── list_tags.xml
│   │   │   │   │   ├── list_tracked_time.xml
│   │   │   │   │   ├── list_user_accounts.xml
│   │   │   │   │   ├── list_users_grid.xml
│   │   │   │   │   ├── list_users_mention.xml
│   │   │   │   │   ├── list_users.xml
│   │   │   │   │   ├── list_wiki.xml
│   │   │   │   │   └── nav_user_accounts.xml
│   │   │   │   ├── menu
│   │   │   │   │   ├── add_new_account_menu.xml
│   │   │   │   │   ├── bottom_nav_menu.xml
│   │   │   │   │   ├── create_issue_menu.xml
│   │   │   │   │   ├── create_label_menu.xml
│   │   │   │   │   ├── create_release_tag_menu.xml
│   │   │   │   │   ├── edit_menu.xml
│   │   │   │   │   ├── file_create_edit_menu.xml
│   │   │   │   │   ├── files_switch_branches_menu.xml
│   │   │   │   │   ├── filter_menu_milestone.xml
│   │   │   │   │   ├── filter_menu_notifications.xml
│   │   │   │   │   ├── filter_menu_pr.xml
│   │   │   │   │   ├── filter_menu_releases.xml
│   │   │   │   │   ├── filter_menu.xml
│   │   │   │   │   ├── generic_nav_dotted_menu.xml
│   │   │   │   │   ├── markdown_switcher.xml
│   │   │   │   │   ├── profile_dotted_menu.xml
│   │   │   │   │   ├── repo_dotted_menu.xml
│   │   │   │   │   ├── reset_menu.xml
│   │   │   │   │   ├── save.xml
│   │   │   │   │   └── search_menu.xml
│   │   │   │   ├── mipmap-anydpi-v26
│   │   │   │   │   ├── app_logo_round.xml
│   │   │   │   │   └── app_logo.xml
│   │   │   │   ├── mipmap-hdpi
│   │   │   │   │   ├── app_logo_foreground.png
│   │   │   │   │   ├── app_logo.png
│   │   │   │   │   └── app_logo_round.png
│   │   │   │   ├── mipmap-mdpi
│   │   │   │   │   ├── app_logo_foreground.png
│   │   │   │   │   ├── app_logo.png
│   │   │   │   │   └── app_logo_round.png
│   │   │   │   ├── mipmap-xhdpi
│   │   │   │   │   ├── app_logo_foreground.png
│   │   │   │   │   ├── app_logo.png
│   │   │   │   │   └── app_logo_round.png
│   │   │   │   ├── mipmap-xxhdpi
│   │   │   │   │   ├── app_logo_foreground.png
│   │   │   │   │   ├── app_logo.png
│   │   │   │   │   └── app_logo_round.png
│   │   │   │   ├── mipmap-xxxhdpi
│   │   │   │   │   ├── app_logo_foreground.png
│   │   │   │   │   ├── app_logo.png
│   │   │   │   │   └── app_logo_round.png
│   │   │   │   ├── navigation
│   │   │   │   │   └── nav_graph.xml
│   │   │   │   ├── values
│   │   │   │   │   ├── attrs.xml
│   │   │   │   │   ├── code_editor_colors.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   ├── gitea_version.xml
│   │   │   │   │   ├── language_statistics_colors.xml
│   │   │   │   │   ├── settings.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── styles.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-ar-rSA
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-cs-rCZ
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-de-rDE
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-el-rGR
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-eo-rEO
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-es-rES
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-fa-rIR
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-fi-rFI
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-fr-rFR
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-it-rIT
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ja-rJP
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-night
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-nl-rNL
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-pl-rPL
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-pt-rBR
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-ru-rRU
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-si-rLK
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sk-rSK
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-sr-rRS
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-tr-rTR
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-uk-rUA
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-v27
│   │   │   │   ├── values-v31
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-zh-rCN
│   │   │   │   │   └── strings.xml
│   │   │   │   ├── values-zh-rTW
│   │   │   │   │   └── strings.xml
│   │   │   │   └── xml
│   │   │   │       ├── changelog.xml
│   │   │   │       └── network_security_config.xml
│   │   │   ├── AndroidManifest.xml
│   │   │   └── app_logo-playstore.png
│   │   └── test
│   │       └── java
│   │           └── org
│   │               └── mian
│   │                   └── gitnex
│   │                       └── helpers
│   │                           ├── AppUtilTest.java
│   │                           ├── ParseDiffTest.java
│   │                           ├── PathsHelperTest.java
│   │                           └── VersionTest.java
│   ├── build.gradle
│   ├── .gitignore
│   └── proguard-rules.pro
├── assets
│   ├── apk-badge.png
│   ├── fdroid.png
│   ├── google-play.png
│   ├── IzzyOnDroid.png
│   ├── license.svg
│   ├── openapk.png
│   └── patreon.png
├── fastlane
│   └── metadata
│       └── android
│           └── en-US
│               ├── changelogs
│               │   ├── 1000.txt
│               │   ├── 100.txt
│               │   ├── 104.txt
│               │   ├── 18.txt
│               │   ├── 22.txt
│               │   ├── 24.txt
│               │   ├── 25.txt
│               │   ├── 26.txt
│               │   ├── 295.txt
│               │   ├── 296.txt
│               │   ├── 297.txt
│               │   ├── 300.txt
│               │   ├── 307.txt
│               │   ├── 308.txt
│               │   ├── 30.txt
│               │   ├── 310.txt
│               │   ├── 317.txt
│               │   ├── 320.txt
│               │   ├── 327.txt
│               │   ├── 328.txt
│               │   ├── 329.txt
│               │   ├── 32.txt
│               │   ├── 330.txt
│               │   ├── 33.txt
│               │   ├── 350.txt
│               │   ├── 35.txt
│               │   ├── 38.txt
│               │   ├── 400.txt
│               │   ├── 410.txt
│               │   ├── 420.txt
│               │   ├── 42.txt
│               │   ├── 430.txt
│               │   ├── 440.txt
│               │   ├── 450.txt
│               │   ├── 45.txt
│               │   ├── 500.txt
│               │   ├── 510.txt
│               │   ├── 520.txt
│               │   ├── 530.txt
│               │   ├── 540.txt
│               │   ├── 550.txt
│               │   ├── 55.txt
│               │   ├── 600.txt
│               │   ├── 60.txt
│               │   ├── 61.txt
│               │   ├── 62.txt
│               │   ├── 63.txt
│               │   ├── 700.txt
│               │   ├── 70.txt
│               │   ├── 800.txt
│               │   ├── 80.txt
│               │   ├── 900.txt
│               │   └── 90.txt
│               ├── images
│               │   ├── phoneScreenshots
│               │   │   ├── 001.png
│               │   │   ├── 002.png
│               │   │   ├── 003.png
│               │   │   ├── 004.png
│               │   │   ├── 005.png
│               │   │   ├── 006.png
│               │   │   ├── 007.png
│               │   │   └── 008.png
│               │   ├── featureGraphic.png
│               │   └── icon.png
│               ├── full_description.txt
│               ├── short_description.txt
│               └── title.txt
├── .gitea
│   ├── issue_template
│   │   ├── bug.md
│   │   ├── feature.md
│   │   ├── question.md
│   │   └── suggestion.md
│   └── pull_request_template.md
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── scripts
│   └── sign-build.sh
├── .woodpecker
│   ├── build.yml
│   ├── check.yml
│   ├── finish.yml
│   └── locale.yml
├── APP_STRUCTURE.md
├── build.gradle
├── CHANGELOG.md
├── crowdin.example.yml
├── .editorconfig
├── .gitattributes
├── .gitignore
├── gradle.properties
├── gradlew
├── gradlew.bat
├── LICENSE
├── local.properties
├── README.md
└── settings.gradle

108 directories, 898 files
