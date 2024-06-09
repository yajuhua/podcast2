package io.github.yajuhua.podcast2.pojo.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GithubActionWorkflowsDTO {

    @SerializedName("total_count")
    private Integer totalCount;
    @SerializedName("workflow_runs")
    private List<WorkflowRunsDTO> workflowRuns;

    @NoArgsConstructor
    @Data
    public static class WorkflowRunsDTO {
        @SerializedName("id")
        private Long id;
        @SerializedName("name")
        private String name;
        @SerializedName("node_id")
        private String nodeId;
        @SerializedName("head_branch")
        private String headBranch;
        @SerializedName("head_sha")
        private String headSha;
        @SerializedName("path")
        private String path;
        @SerializedName("display_title")
        private String displayTitle;
        @SerializedName("run_number")
        private Integer runNumber;
        @SerializedName("event")
        private String event;
        @SerializedName("status")
        private String status;
        @SerializedName("conclusion")
        private String conclusion;
        @SerializedName("workflow_id")
        private Integer workflowId;
        @SerializedName("check_suite_id")
        private Long checkSuiteId;
        @SerializedName("check_suite_node_id")
        private String checkSuiteNodeId;
        @SerializedName("url")
        private String url;
        @SerializedName("html_url")
        private String htmlUrl;
        @SerializedName("pull_requests")
        private List<?> pullRequests;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("updated_at")
        private String updatedAt;
        @SerializedName("actor")
        private ActorDTO actor;
        @SerializedName("run_attempt")
        private Integer runAttempt;
        @SerializedName("referenced_workflows")
        private List<?> referencedWorkflows;
        @SerializedName("run_started_at")
        private String runStartedAt;
        @SerializedName("triggering_actor")
        private TriggeringActorDTO triggeringActor;
        @SerializedName("jobs_url")
        private String jobsUrl;
        @SerializedName("logs_url")
        private String logsUrl;
        @SerializedName("check_suite_url")
        private String checkSuiteUrl;
        @SerializedName("artifacts_url")
        private String artifactsUrl;
        @SerializedName("cancel_url")
        private String cancelUrl;
        @SerializedName("rerun_url")
        private String rerunUrl;
        @SerializedName("previous_attempt_url")
        private Object previousAttemptUrl;
        @SerializedName("workflow_url")
        private String workflowUrl;
        @SerializedName("head_commit")
        private HeadCommitDTO headCommit;
        @SerializedName("repository")
        private RepositoryDTO repository;
        @SerializedName("head_repository")
        private HeadRepositoryDTO headRepository;

        @NoArgsConstructor
        @Data
        public static class ActorDTO {
            @SerializedName("login")
            private String login;
            @SerializedName("id")
            private Integer id;
            @SerializedName("node_id")
            private String nodeId;
            @SerializedName("avatar_url")
            private String avatarUrl;
            @SerializedName("gravatar_id")
            private String gravatarId;
            @SerializedName("url")
            private String url;
            @SerializedName("html_url")
            private String htmlUrl;
            @SerializedName("followers_url")
            private String followersUrl;
            @SerializedName("following_url")
            private String followingUrl;
            @SerializedName("gists_url")
            private String gistsUrl;
            @SerializedName("starred_url")
            private String starredUrl;
            @SerializedName("subscriptions_url")
            private String subscriptionsUrl;
            @SerializedName("organizations_url")
            private String organizationsUrl;
            @SerializedName("repos_url")
            private String reposUrl;
            @SerializedName("events_url")
            private String eventsUrl;
            @SerializedName("received_events_url")
            private String receivedEventsUrl;
            @SerializedName("type")
            private String type;
            @SerializedName("site_admin")
            private Boolean siteAdmin;
        }

        @NoArgsConstructor
        @Data
        public static class TriggeringActorDTO {
            @SerializedName("login")
            private String login;
            @SerializedName("id")
            private Integer id;
            @SerializedName("node_id")
            private String nodeId;
            @SerializedName("avatar_url")
            private String avatarUrl;
            @SerializedName("gravatar_id")
            private String gravatarId;
            @SerializedName("url")
            private String url;
            @SerializedName("html_url")
            private String htmlUrl;
            @SerializedName("followers_url")
            private String followersUrl;
            @SerializedName("following_url")
            private String followingUrl;
            @SerializedName("gists_url")
            private String gistsUrl;
            @SerializedName("starred_url")
            private String starredUrl;
            @SerializedName("subscriptions_url")
            private String subscriptionsUrl;
            @SerializedName("organizations_url")
            private String organizationsUrl;
            @SerializedName("repos_url")
            private String reposUrl;
            @SerializedName("events_url")
            private String eventsUrl;
            @SerializedName("received_events_url")
            private String receivedEventsUrl;
            @SerializedName("type")
            private String type;
            @SerializedName("site_admin")
            private Boolean siteAdmin;
        }

        @NoArgsConstructor
        @Data
        public static class HeadCommitDTO {
            @SerializedName("id")
            private String id;
            @SerializedName("tree_id")
            private String treeId;
            @SerializedName("message")
            private String message;
            @SerializedName("timestamp")
            private String timestamp;
            @SerializedName("author")
            private AuthorDTO author;
            @SerializedName("committer")
            private CommitterDTO committer;

            @NoArgsConstructor
            @Data
            public static class AuthorDTO {
                @SerializedName("name")
                private String name;
                @SerializedName("email")
                private String email;
            }

            @NoArgsConstructor
            @Data
            public static class CommitterDTO {
                @SerializedName("name")
                private String name;
                @SerializedName("email")
                private String email;
            }
        }

        @NoArgsConstructor
        @Data
        public static class RepositoryDTO {
            @SerializedName("id")
            private Integer id;
            @SerializedName("node_id")
            private String nodeId;
            @SerializedName("name")
            private String name;
            @SerializedName("full_name")
            private String fullName;
            @SerializedName("private")
            private Boolean privateX;
            @SerializedName("owner")
            private OwnerDTO owner;
            @SerializedName("html_url")
            private String htmlUrl;
            @SerializedName("description")
            private String description;
            @SerializedName("fork")
            private Boolean fork;
            @SerializedName("url")
            private String url;
            @SerializedName("forks_url")
            private String forksUrl;
            @SerializedName("keys_url")
            private String keysUrl;
            @SerializedName("collaborators_url")
            private String collaboratorsUrl;
            @SerializedName("teams_url")
            private String teamsUrl;
            @SerializedName("hooks_url")
            private String hooksUrl;
            @SerializedName("issue_events_url")
            private String issueEventsUrl;
            @SerializedName("events_url")
            private String eventsUrl;
            @SerializedName("assignees_url")
            private String assigneesUrl;
            @SerializedName("branches_url")
            private String branchesUrl;
            @SerializedName("tags_url")
            private String tagsUrl;
            @SerializedName("blobs_url")
            private String blobsUrl;
            @SerializedName("git_tags_url")
            private String gitTagsUrl;
            @SerializedName("git_refs_url")
            private String gitRefsUrl;
            @SerializedName("trees_url")
            private String treesUrl;
            @SerializedName("statuses_url")
            private String statusesUrl;
            @SerializedName("languages_url")
            private String languagesUrl;
            @SerializedName("stargazers_url")
            private String stargazersUrl;
            @SerializedName("contributors_url")
            private String contributorsUrl;
            @SerializedName("subscribers_url")
            private String subscribersUrl;
            @SerializedName("subscription_url")
            private String subscriptionUrl;
            @SerializedName("commits_url")
            private String commitsUrl;
            @SerializedName("git_commits_url")
            private String gitCommitsUrl;
            @SerializedName("comments_url")
            private String commentsUrl;
            @SerializedName("issue_comment_url")
            private String issueCommentUrl;
            @SerializedName("contents_url")
            private String contentsUrl;
            @SerializedName("compare_url")
            private String compareUrl;
            @SerializedName("merges_url")
            private String mergesUrl;
            @SerializedName("archive_url")
            private String archiveUrl;
            @SerializedName("downloads_url")
            private String downloadsUrl;
            @SerializedName("issues_url")
            private String issuesUrl;
            @SerializedName("pulls_url")
            private String pullsUrl;
            @SerializedName("milestones_url")
            private String milestonesUrl;
            @SerializedName("notifications_url")
            private String notificationsUrl;
            @SerializedName("labels_url")
            private String labelsUrl;
            @SerializedName("releases_url")
            private String releasesUrl;
            @SerializedName("deployments_url")
            private String deploymentsUrl;

            @NoArgsConstructor
            @Data
            public static class OwnerDTO {
                @SerializedName("login")
                private String login;
                @SerializedName("id")
                private Integer id;
                @SerializedName("node_id")
                private String nodeId;
                @SerializedName("avatar_url")
                private String avatarUrl;
                @SerializedName("gravatar_id")
                private String gravatarId;
                @SerializedName("url")
                private String url;
                @SerializedName("html_url")
                private String htmlUrl;
                @SerializedName("followers_url")
                private String followersUrl;
                @SerializedName("following_url")
                private String followingUrl;
                @SerializedName("gists_url")
                private String gistsUrl;
                @SerializedName("starred_url")
                private String starredUrl;
                @SerializedName("subscriptions_url")
                private String subscriptionsUrl;
                @SerializedName("organizations_url")
                private String organizationsUrl;
                @SerializedName("repos_url")
                private String reposUrl;
                @SerializedName("events_url")
                private String eventsUrl;
                @SerializedName("received_events_url")
                private String receivedEventsUrl;
                @SerializedName("type")
                private String type;
                @SerializedName("site_admin")
                private Boolean siteAdmin;
            }
        }

        @NoArgsConstructor
        @Data
        public static class HeadRepositoryDTO {
            @SerializedName("id")
            private Integer id;
            @SerializedName("node_id")
            private String nodeId;
            @SerializedName("name")
            private String name;
            @SerializedName("full_name")
            private String fullName;
            @SerializedName("private")
            private Boolean privateX;
            @SerializedName("owner")
            private OwnerDTO owner;
            @SerializedName("html_url")
            private String htmlUrl;
            @SerializedName("description")
            private String description;
            @SerializedName("fork")
            private Boolean fork;
            @SerializedName("url")
            private String url;
            @SerializedName("forks_url")
            private String forksUrl;
            @SerializedName("keys_url")
            private String keysUrl;
            @SerializedName("collaborators_url")
            private String collaboratorsUrl;
            @SerializedName("teams_url")
            private String teamsUrl;
            @SerializedName("hooks_url")
            private String hooksUrl;
            @SerializedName("issue_events_url")
            private String issueEventsUrl;
            @SerializedName("events_url")
            private String eventsUrl;
            @SerializedName("assignees_url")
            private String assigneesUrl;
            @SerializedName("branches_url")
            private String branchesUrl;
            @SerializedName("tags_url")
            private String tagsUrl;
            @SerializedName("blobs_url")
            private String blobsUrl;
            @SerializedName("git_tags_url")
            private String gitTagsUrl;
            @SerializedName("git_refs_url")
            private String gitRefsUrl;
            @SerializedName("trees_url")
            private String treesUrl;
            @SerializedName("statuses_url")
            private String statusesUrl;
            @SerializedName("languages_url")
            private String languagesUrl;
            @SerializedName("stargazers_url")
            private String stargazersUrl;
            @SerializedName("contributors_url")
            private String contributorsUrl;
            @SerializedName("subscribers_url")
            private String subscribersUrl;
            @SerializedName("subscription_url")
            private String subscriptionUrl;
            @SerializedName("commits_url")
            private String commitsUrl;
            @SerializedName("git_commits_url")
            private String gitCommitsUrl;
            @SerializedName("comments_url")
            private String commentsUrl;
            @SerializedName("issue_comment_url")
            private String issueCommentUrl;
            @SerializedName("contents_url")
            private String contentsUrl;
            @SerializedName("compare_url")
            private String compareUrl;
            @SerializedName("merges_url")
            private String mergesUrl;
            @SerializedName("archive_url")
            private String archiveUrl;
            @SerializedName("downloads_url")
            private String downloadsUrl;
            @SerializedName("issues_url")
            private String issuesUrl;
            @SerializedName("pulls_url")
            private String pullsUrl;
            @SerializedName("milestones_url")
            private String milestonesUrl;
            @SerializedName("notifications_url")
            private String notificationsUrl;
            @SerializedName("labels_url")
            private String labelsUrl;
            @SerializedName("releases_url")
            private String releasesUrl;
            @SerializedName("deployments_url")
            private String deploymentsUrl;

            @NoArgsConstructor
            @Data
            public static class OwnerDTO {
                @SerializedName("login")
                private String login;
                @SerializedName("id")
                private Integer id;
                @SerializedName("node_id")
                private String nodeId;
                @SerializedName("avatar_url")
                private String avatarUrl;
                @SerializedName("gravatar_id")
                private String gravatarId;
                @SerializedName("url")
                private String url;
                @SerializedName("html_url")
                private String htmlUrl;
                @SerializedName("followers_url")
                private String followersUrl;
                @SerializedName("following_url")
                private String followingUrl;
                @SerializedName("gists_url")
                private String gistsUrl;
                @SerializedName("starred_url")
                private String starredUrl;
                @SerializedName("subscriptions_url")
                private String subscriptionsUrl;
                @SerializedName("organizations_url")
                private String organizationsUrl;
                @SerializedName("repos_url")
                private String reposUrl;
                @SerializedName("events_url")
                private String eventsUrl;
                @SerializedName("received_events_url")
                private String receivedEventsUrl;
                @SerializedName("type")
                private String type;
                @SerializedName("site_admin")
                private Boolean siteAdmin;
            }
        }
    }
}
