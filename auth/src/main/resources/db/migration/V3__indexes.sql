CREATE INDEX IF NOT EXISTS idx_tasks_project_id_status ON tasks (project_id, status);
CREATE INDEX IF NOT EXISTS idx_task_activities_task_id ON task_activities (task_id);
CREATE INDEX IF NOT EXISTS idx_task_activities_occurred_at ON task_activities (occurred_at);
