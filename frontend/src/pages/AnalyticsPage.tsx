import { useEffect, useState } from 'react';
import { Card } from '@/components/ui/Card';
import { Loading } from '@/components/ui/Loading';
import { Alert } from '@/components/ui/Alert';
import { useTasks } from '@/hooks/useTasks';
import { useProjects } from '@/hooks/useProjects';
import { Task, TaskStatus, Project } from '@/types';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line
} from 'recharts';

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6'];

export const AnalyticsPage = () => {
  const { tasks, loading: tasksLoading, refetch } = useTasks();
  const { projects, loading: projectsLoading } = useProjects();
  
  const [tasksByStatus, setTasksByStatus] = useState<Array<{name: string; value: number; status: string}>>([]);
  const [tasksByPriority, setTasksByPriority] = useState<Array<{name: string; value: number; priority: string}>>([]);
  const [tasksByProject, setTasksByProject] = useState<Array<{name: string; value: number; projectId: string}>>([]);
  const [taskTrend, setTaskTrend] = useState<Array<{date: string; 'Tạo mới': number; 'Hoàn thành': number}>>([]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  useEffect(() => {
    if (tasks && tasks.length > 0) {
      // Tasks by Status
      const statusGroups = tasks.reduce((acc: Record<string, number>, task: Task) => {
        const status = task.status || 'TODO';
        acc[status] = (acc[status] || 0) + 1;
        return acc;
      }, {});
      
      const statusData = Object.entries(statusGroups).map(([name, value]) => ({
        name: getStatusLabel(name as TaskStatus),
        value: value as number,
        status: name
      }));
      setTasksByStatus(statusData);

      // Tasks by Priority
      const priorityGroups = tasks.reduce((acc: Record<string, number>, task: Task) => {
        const priority = task.priority || 'MEDIUM';
        acc[priority] = (acc[priority] || 0) + 1;
        return acc;
      }, {});
      
      const priorityData = Object.entries(priorityGroups).map(([name, value]) => ({
        name: getPriorityLabel(name),
        value: value as number,
        priority: name
      }));
      setTasksByPriority(priorityData);

      // Tasks by Project
      if (projects && projects.length > 0) {
        const projectGroups = tasks.reduce((acc: Record<string, number>, task: Task) => {
          const projectId = String(task.projectId || 'no-project');
          acc[projectId] = (acc[projectId] || 0) + 1;
          return acc;
        }, {});
        
        const projectData = Object.entries(projectGroups).map(([projectId, count]) => {
          const project = projects.find((p: Project) => p.id === Number(projectId));
          return {
            name: project?.name || 'Không có dự án',
            value: count as number,
            projectId
          };
        }).sort((a, b) => b.value - a.value).slice(0, 10);
        
        setTasksByProject(projectData);
      }

      // Task Trend (last 7 days)
      const last7Days = Array.from({ length: 7 }, (_, i) => {
        const date = new Date();
        date.setDate(date.getDate() - (6 - i));
        return date.toISOString().split('T')[0];
      });

      const trendData = last7Days.map(date => {
        const created = tasks.filter((t: Task) => 
          t.createdAt && t.createdAt.startsWith(date)
        ).length;
        
        const completed = tasks.filter((t: Task) => 
          t.updatedAt && t.updatedAt.startsWith(date) && t.status === TaskStatus.DONE
        ).length;

        return {
          date: new Date(date).toLocaleDateString('vi-VN', { month: 'short', day: 'numeric' }),
          'Tạo mới': created,
          'Hoàn thành': completed
        };
      });
      
      setTaskTrend(trendData);
    }
  }, [tasks, projects]);

  const getStatusLabel = (status: TaskStatus): string => {
    const labels: Record<string, string> = {
      [TaskStatus.TODO]: 'Chờ xử lý',
      [TaskStatus.IN_PROGRESS]: 'Đang làm',
      [TaskStatus.DONE]: 'Hoàn thành',
      [TaskStatus.CANCELLED]: 'Đã hủy',
    };
    return labels[status] || status;
  };

  const getPriorityLabel = (priority: string): string => {
    const labels: Record<string, string> = {
      'HIGH': 'Cao',
      'MEDIUM': 'Trung bình',
      'LOW': 'Thấp',
      'NORMAL': 'Bình thường',
    };
    return labels[priority] || priority;
  };

  if (tasksLoading || projectsLoading) {
    return <Loading />;
  }

  if (!tasks || tasks.length === 0) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Phân tích & Thống kê</h1>
          <p className="text-gray-600 mt-2">Theo dõi hiệu suất và tiến độ công việc</p>
        </div>
        <Alert type="info">
          Chưa có dữ liệu để phân tích. Hãy tạo task và dự án để xem thống kê!
        </Alert>
      </div>
    );
  }

  const completionRate = tasks.length > 0 
    ? Math.round((tasks.filter((t: Task) => t.status === TaskStatus.DONE).length / tasks.length) * 100)
    : 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Phân tích & Thống kê</h1>
        <p className="text-gray-600 mt-2">Theo dõi hiệu suất và tiến độ công việc</p>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <div className="p-6">
            <p className="text-sm text-gray-600">Tổng số Task</p>
            <p className="text-3xl font-bold text-gray-900 mt-2">{tasks.length}</p>
            <div className="mt-2 text-sm text-gray-500">
              Tất cả task trong hệ thống
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <p className="text-sm text-gray-600">Tỷ lệ hoàn thành</p>
            <p className="text-3xl font-bold text-green-600 mt-2">{completionRate}%</p>
            <div className="mt-2 text-sm text-gray-500">
              {tasks.filter((t: Task) => t.status === TaskStatus.DONE).length} / {tasks.length} task
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <p className="text-sm text-gray-600">Đang thực hiện</p>
            <p className="text-3xl font-bold text-yellow-600 mt-2">
              {tasks.filter((t: Task) => t.status === TaskStatus.IN_PROGRESS).length}
            </p>
            <div className="mt-2 text-sm text-gray-500">
              Task đang được làm
            </div>
          </div>
        </Card>

        <Card>
          <div className="p-6">
            <p className="text-sm text-gray-600">Tổng Dự án</p>
            <p className="text-3xl font-bold text-purple-600 mt-2">{projects?.length || 0}</p>
            <div className="mt-2 text-sm text-gray-500">
              Dự án đang hoạt động
            </div>
          </div>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Task Status Chart */}
        <Card>
          <div className="p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Phân bố theo Trạng thái</h2>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={tasksByStatus}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {tasksByStatus.map((_entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Task Priority Chart */}
        <Card>
          <div className="p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Phân bố theo Độ ưu tiên</h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={tasksByPriority}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="value" fill="#3B82F6" name="Số lượng" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Project Distribution */}
      {tasksByProject.length > 0 && (
        <Card>
          <div className="p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Phân bố Task theo Dự án (Top 10)</h2>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={tasksByProject} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" />
                <YAxis dataKey="name" type="category" width={150} />
                <Tooltip />
                <Legend />
                <Bar dataKey="value" fill="#8B5CF6" name="Số task" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      {/* Task Trend */}
      <Card>
        <div className="p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Xu hướng Task (7 ngày qua)</h2>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={taskTrend}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="Tạo mới" stroke="#3B82F6" strokeWidth={2} />
              <Line type="monotone" dataKey="Hoàn thành" stroke="#10B981" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </Card>
    </div>
  );
};
