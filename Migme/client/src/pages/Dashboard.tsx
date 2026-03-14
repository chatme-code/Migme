import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Shield,
  ArrowUpCircle,
  Package,
  Trash2,
  PlusCircle,
  AlertTriangle,
  CheckCircle2,
  Code2,
  GitCompare,
  Layers,
  ChevronDown,
  ChevronRight,
  Wrench,
  Database,
  Server,
  Cloud,
  Zap,
  Lock,
  TestTube,
  Globe,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as ReTooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";

interface Dependency {
  id: number;
  category: string;
  name: string;
  oldCoord: string;
  newCoord: string;
  oldVersion: string;
  newVersion: string;
  status: string;
  reason: string;
  breaking: boolean;
  cveIds: string[];
}

interface CodeChange {
  file: string;
  changes: string[];
  effort: string;
}

interface ProjectInfo {
  groupId: string;
  artifactId: string;
  oldVersion: string;
  newVersion: string;
  oldJava: string;
  newJava: string;
  oldSpring: string;
  newSpring: string;
  buildSystem: string;
  scm: string;
}

interface Summary {
  totalDependencies: number;
  updated: number;
  removed: number;
  added: number;
  securityFixes: number;
  majorUpgrades: number;
  minorUpgrades: number;
}

const STATUS_CONFIG: Record<string, { label: string; color: string; bg: string; icon: typeof Shield }> = {
  critical: { label: "Critical / Security", color: "text-red-400", bg: "bg-red-500/10 border-red-500/30", icon: Shield },
  major:    { label: "Major Upgrade",        color: "text-amber-400", bg: "bg-amber-500/10 border-amber-500/30", icon: ArrowUpCircle },
  minor:    { label: "Minor Upgrade",        color: "text-blue-400",  bg: "bg-blue-500/10 border-blue-500/30",  icon: ArrowUpCircle },
  removed:  { label: "Removed",             color: "text-slate-400", bg: "bg-slate-500/10 border-slate-500/30", icon: Trash2 },
  added:    { label: "Added (new)",          color: "text-green-400", bg: "bg-green-500/10 border-green-500/30", icon: PlusCircle },
};

const CATEGORY_ICONS: Record<string, typeof Shield> = {
  Logging: Zap,
  Cloud: Cloud,
  Database: Database,
  Cache: Server,
  Redis: Database,
  Framework: Layers,
  API: Globe,
  JSON: Code2,
  Messaging: Zap,
  Distributed: Server,
  SSH: Lock,
  XMPP: Globe,
  HTTP: Globe,
  Utilities: Package,
  Security: Lock,
  Scheduling: Zap,
  Compression: Package,
  Middleware: Server,
  Testing: TestTube,
  Build: Wrench,
  "Big Data": Database,
};

const CHART_COLORS = {
  critical: "#f87171",
  major: "#fbbf24",
  minor: "#60a5fa",
  removed: "#94a3b8",
  added: "#4ade80",
};

function StatusBadge({ status }: { status: string }) {
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG["minor"];
  const Icon = cfg.icon;
  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium border ${cfg.bg} ${cfg.color}`}
    >
      <Icon className="w-3 h-3" />
      {cfg.label}
    </span>
  );
}

function EffortBadge({ effort }: { effort: string }) {
  const cfg: Record<string, string> = {
    Low: "bg-green-500/10 text-green-400 border-green-500/30",
    Medium: "bg-amber-500/10 text-amber-400 border-amber-500/30",
    High: "bg-red-500/10 text-red-400 border-red-500/30",
  };
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${cfg[effort] ?? ""}`}>
      {effort} effort
    </span>
  );
}

function DependencyRow({ dep, expanded, onToggle }: { dep: Dependency; expanded: boolean; onToggle: () => void }) {
  const CategoryIcon = CATEGORY_ICONS[dep.category] ?? Package;
  return (
    <div
      className="border border-border rounded-lg overflow-hidden transition-all duration-200"
      data-testid={`dep-row-${dep.id}`}
    >
      <button
        onClick={onToggle}
        className="w-full flex items-center gap-3 p-4 text-left hover:bg-accent/40 transition-colors"
        data-testid={`dep-toggle-${dep.id}`}
      >
        <span className="flex-shrink-0 w-7 h-7 rounded-md bg-muted flex items-center justify-center">
          <CategoryIcon className="w-3.5 h-3.5 text-muted-foreground" />
        </span>

        <span className="flex-1 min-w-0">
          <span className="block font-medium text-sm truncate">{dep.name}</span>
          <span className="block text-xs text-muted-foreground truncate">
            {dep.category}
          </span>
        </span>

        <span className="hidden sm:flex items-center gap-2 flex-shrink-0">
          <span className="text-xs font-mono text-muted-foreground bg-muted px-2 py-0.5 rounded line-through">
            {dep.oldVersion}
          </span>
          <span className="text-muted-foreground">→</span>
          <span className="text-xs font-mono text-foreground bg-primary/10 px-2 py-0.5 rounded">
            {dep.newVersion}
          </span>
        </span>

        <span className="flex-shrink-0">
          <StatusBadge status={dep.status} />
        </span>

        <span className="flex-shrink-0 text-muted-foreground">
          {expanded ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
        </span>
      </button>

      {expanded && (
        <div className="px-4 pb-4 pt-0 border-t border-border bg-muted/20">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div>
              <p className="text-xs font-semibold text-muted-foreground mb-1 uppercase tracking-wider">Old Coordinate</p>
              <code className="text-xs font-mono text-red-400 bg-red-500/5 border border-red-500/20 px-2 py-1 rounded block break-all">
                {dep.oldCoord}
              </code>
            </div>
            <div>
              <p className="text-xs font-semibold text-muted-foreground mb-1 uppercase tracking-wider">New Coordinate</p>
              <code className="text-xs font-mono text-green-400 bg-green-500/5 border border-green-500/20 px-2 py-1 rounded block break-all">
                {dep.newCoord}
              </code>
            </div>
          </div>

          <div className="mt-3">
            <p className="text-xs font-semibold text-muted-foreground mb-1 uppercase tracking-wider">Reason</p>
            <p className="text-sm text-foreground/80">{dep.reason}</p>
          </div>

          <div className="mt-3 flex flex-wrap gap-2">
            {dep.breaking && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-orange-500/10 text-orange-400 border border-orange-500/30">
                <AlertTriangle className="w-3 h-3" /> Breaking change
              </span>
            )}
            {dep.cveIds.map((cve) => (
              <span
                key={cve}
                className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-red-500/10 text-red-400 border border-red-500/30"
              >
                <Shield className="w-3 h-3" /> {cve}
              </span>
            ))}
            {!dep.breaking && dep.cveIds.length === 0 && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-green-500/10 text-green-400 border border-green-500/30">
                <CheckCircle2 className="w-3 h-3" /> Non-breaking
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default function Dashboard() {
  const [categoryFilter, setCategoryFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [expandedDeps, setExpandedDeps] = useState<Set<number>>(new Set());

  const { data: analysisData, isLoading } = useQuery<{
    projectInfo: ProjectInfo;
    summary: Summary;
    dependencies: Dependency[];
    codeChanges: CodeChange[];
    plugins: { removed: any[]; added: any[] };
  }>({
    queryKey: ["/api/analysis"],
  });

  const toggleDep = (id: number) => {
    setExpandedDeps((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const deps = analysisData?.dependencies ?? [];
  const filtered = deps.filter((d) => {
    const catOk = categoryFilter === "all" || d.category.toLowerCase() === categoryFilter;
    const stOk = statusFilter === "all" || d.status === statusFilter;
    return catOk && stOk;
  });

  const categories = ["all", ...Array.from(new Set(deps.map((d) => d.category.toLowerCase())))];
  const summary = analysisData?.summary;
  const info = analysisData?.projectInfo;

  const pieData = summary
    ? [
        { name: "Major upgrades", value: summary.majorUpgrades, key: "major" },
        { name: "Minor upgrades", value: summary.minorUpgrades, key: "minor" },
        { name: "Removed", value: summary.removed, key: "removed" },
        { name: "Added (new)", value: summary.added, key: "added" },
        { name: "Security fixes", value: summary.securityFixes, key: "critical" },
      ]
    : [];

  const barData = Array.from(
    deps.reduce((acc, d) => {
      const existing = acc.get(d.category) ?? { category: d.category, updated: 0, removed: 0, added: 0, critical: 0 };
      if (d.status === "removed") existing.removed++;
      else if (d.status === "added") existing.added++;
      else if (d.status === "critical") existing.critical++;
      else existing.updated++;
      acc.set(d.category, existing);
      return acc;
    }, new Map<string, any>())
  )
    .map(([, v]) => v)
    .sort((a, b) => (b.updated + b.critical) - (a.updated + a.critical));

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-muted-foreground">Loading analysis…</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background dark">
      {/* ── Header ─────────────────────────────────── */}
      <header className="border-b border-border bg-card/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center flex-shrink-0">
                <GitCompare className="w-4 h-4 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-base font-bold leading-tight">Fusion Modernisation</h1>
                <p className="text-xs text-muted-foreground leading-tight hidden sm:block">
                  Java 1.5 → Java 21 · Spring 2.5.5 → Boot 3.3.1
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="hidden md:flex items-center gap-1 text-xs text-muted-foreground bg-muted px-3 py-1.5 rounded-full border border-border">
                <Package className="w-3 h-3" />
                com.projectgoth / Fusion
              </span>
              <span className="flex items-center gap-1 text-xs font-mono text-amber-400 bg-amber-500/10 px-2 py-1 rounded border border-amber-500/30">
                v8.75.132 → v9.0.0
              </span>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">

        {/* ── Hero Version Banner ───────────────────── */}
        <div className="relative overflow-hidden rounded-xl border border-border bg-gradient-to-br from-primary/10 via-card to-card p-6 md:p-8">
          <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full -translate-y-32 translate-x-32 pointer-events-none" />
          <div className="relative grid grid-cols-1 md:grid-cols-3 gap-6 md:gap-8">
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Java Version</p>
              <div className="flex items-center gap-3">
                <div className="text-center">
                  <div className="text-2xl font-bold text-red-400 font-mono">{info?.oldJava}</div>
                  <div className="text-xs text-muted-foreground mt-0.5">Legacy (EOL)</div>
                </div>
                <ArrowUpCircle className="w-5 h-5 text-muted-foreground flex-shrink-0" />
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-400 font-mono">{info?.newJava}</div>
                  <div className="text-xs text-muted-foreground mt-0.5">LTS · Active</div>
                </div>
              </div>
            </div>
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Framework</p>
              <div className="flex items-center gap-3">
                <div className="text-center">
                  <div className="text-lg font-bold text-red-400 font-mono">Spring {info?.oldSpring}</div>
                  <div className="text-xs text-muted-foreground mt-0.5">~2007 release</div>
                </div>
                <ArrowUpCircle className="w-5 h-5 text-muted-foreground flex-shrink-0" />
                <div className="text-center">
                  <div className="text-lg font-bold text-green-400 font-mono leading-tight">Boot 3.3.1</div>
                  <div className="text-xs text-muted-foreground mt-0.5">Jakarta EE 10</div>
                </div>
              </div>
            </div>
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">Build System</p>
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-green-400 flex-shrink-0" />
                  <span className="text-sm">Maven → Spring Boot Maven Plugin</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-green-400 flex-shrink-0" />
                  <span className="text-sm">OWASP CVE scanning added</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-green-400 flex-shrink-0" />
                  <span className="text-sm">Versions plugin for drift alerts</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ── Summary Cards ────────────────────────── */}
        {summary && (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {[
              { label: "Total Deps",     value: summary.totalDependencies, icon: Package,      color: "text-blue-400",  bg: "bg-blue-500/10 border-blue-500/20" },
              { label: "Updated",        value: summary.updated,           icon: ArrowUpCircle, color: "text-green-400", bg: "bg-green-500/10 border-green-500/20" },
              { label: "Removed",        value: summary.removed,           icon: Trash2,        color: "text-slate-400", bg: "bg-slate-500/10 border-slate-500/20" },
              { label: "Added New",      value: summary.added,             icon: PlusCircle,    color: "text-emerald-400", bg: "bg-emerald-500/10 border-emerald-500/20" },
              { label: "Security Fixes", value: summary.securityFixes,     icon: Shield,        color: "text-red-400",  bg: "bg-red-500/10 border-red-500/20" },
              { label: "Major Upgrades", value: summary.majorUpgrades,     icon: ArrowUpCircle, color: "text-amber-400", bg: "bg-amber-500/10 border-amber-500/20" },
            ].map(({ label, value, icon: Icon, color, bg }) => (
              <Card key={label} className={`border ${bg} bg-transparent`} data-testid={`stat-${label.replace(/\s/g,"-").toLowerCase()}`}>
                <CardContent className="p-4">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className={`text-2xl font-bold font-mono ${color}`}>{value}</div>
                      <div className="text-xs text-muted-foreground mt-0.5 leading-tight">{label}</div>
                    </div>
                    <Icon className={`w-4 h-4 mt-0.5 ${color} opacity-70`} />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* ── Tabs ─────────────────────────────────── */}
        <Tabs defaultValue="dependencies" className="w-full">
          <TabsList className="bg-muted/50 border border-border w-full md:w-auto grid grid-cols-4 md:flex md:flex-row">
            <TabsTrigger value="dependencies" className="gap-1.5 text-xs sm:text-sm" data-testid="tab-dependencies">
              <Package className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Dependencies</span>
              <span className="sm:hidden">Deps</span>
            </TabsTrigger>
            <TabsTrigger value="charts" className="gap-1.5 text-xs sm:text-sm" data-testid="tab-charts">
              <Layers className="w-3.5 h-3.5" />
              Charts
            </TabsTrigger>
            <TabsTrigger value="code" className="gap-1.5 text-xs sm:text-sm" data-testid="tab-code">
              <Code2 className="w-3.5 h-3.5" />
              Code
            </TabsTrigger>
            <TabsTrigger value="plugins" className="gap-1.5 text-xs sm:text-sm" data-testid="tab-plugins">
              <Wrench className="w-3.5 h-3.5" />
              Plugins
            </TabsTrigger>
          </TabsList>

          {/* ── Dependencies Tab ─────────────────── */}
          <TabsContent value="dependencies" className="mt-6 space-y-4">
            <div className="flex flex-col sm:flex-row gap-3">
              <Select value={categoryFilter} onValueChange={setCategoryFilter}>
                <SelectTrigger className="w-full sm:w-48" data-testid="select-category">
                  <SelectValue placeholder="Category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((c) => (
                    <SelectItem key={c} value={c}>
                      {c === "all" ? "All categories" : c.charAt(0).toUpperCase() + c.slice(1)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-full sm:w-48" data-testid="select-status">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All statuses</SelectItem>
                  <SelectItem value="critical">Critical / Security</SelectItem>
                  <SelectItem value="major">Major Upgrade</SelectItem>
                  <SelectItem value="minor">Minor Upgrade</SelectItem>
                  <SelectItem value="removed">Removed</SelectItem>
                  <SelectItem value="added">Added (new)</SelectItem>
                </SelectContent>
              </Select>

              <span className="flex items-center text-sm text-muted-foreground sm:ml-auto">
                {filtered.length} of {deps.length} shown
              </span>
            </div>

            <div className="space-y-2">
              {filtered.length === 0 && (
                <div className="text-center py-12 text-muted-foreground">
                  No dependencies match the selected filters.
                </div>
              )}
              {filtered.map((dep) => (
                <DependencyRow
                  key={dep.id}
                  dep={dep}
                  expanded={expandedDeps.has(dep.id)}
                  onToggle={() => toggleDep(dep.id)}
                />
              ))}
            </div>
          </TabsContent>

          {/* ── Charts Tab ───────────────────────── */}
          <TabsContent value="charts" className="mt-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Pie Chart */}
              <Card className="border-border">
                <CardHeader>
                  <CardTitle className="text-base">Change Distribution</CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={280}>
                    <PieChart>
                      <Pie
                        data={pieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={100}
                        paddingAngle={3}
                        dataKey="value"
                      >
                        {pieData.map((entry, i) => (
                          <Cell
                            key={i}
                            fill={CHART_COLORS[entry.key as keyof typeof CHART_COLORS]}
                            opacity={0.85}
                          />
                        ))}
                      </Pie>
                      <ReTooltip
                        contentStyle={{
                          backgroundColor: "hsl(222,47%,14%)",
                          border: "1px solid hsl(217,27%,22%)",
                          borderRadius: "8px",
                          fontSize: "12px",
                          color: "hsl(215,25%,97%)",
                        }}
                      />
                      <Legend
                        formatter={(value) => (
                          <span style={{ color: "hsl(215,20%,65%)", fontSize: "12px" }}>{value}</span>
                        )}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* Bar Chart */}
              <Card className="border-border">
                <CardHeader>
                  <CardTitle className="text-base">Changes by Category</CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart data={barData} layout="vertical" margin={{ left: 8, right: 8 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="hsl(217,27%,20%)" horizontal={false} />
                      <XAxis type="number" tick={{ fill: "hsl(215,20%,60%)", fontSize: 11 }} />
                      <YAxis
                        type="category"
                        dataKey="category"
                        width={80}
                        tick={{ fill: "hsl(215,20%,65%)", fontSize: 10 }}
                      />
                      <ReTooltip
                        contentStyle={{
                          backgroundColor: "hsl(222,47%,14%)",
                          border: "1px solid hsl(217,27%,22%)",
                          borderRadius: "8px",
                          fontSize: "12px",
                          color: "hsl(215,25%,97%)",
                        }}
                      />
                      <Bar dataKey="critical" stackId="a" fill={CHART_COLORS.critical} name="Critical" />
                      <Bar dataKey="updated"  stackId="a" fill={CHART_COLORS.major}    name="Updated"  />
                      <Bar dataKey="removed"  stackId="a" fill={CHART_COLORS.removed}  name="Removed"  />
                      <Bar dataKey="added"    stackId="a" fill={CHART_COLORS.added}     name="Added"    radius={[0, 4, 4, 0]} />
                      <Legend
                        formatter={(value) => (
                          <span style={{ color: "hsl(215,20%,65%)", fontSize: "12px" }}>{value}</span>
                        )}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* Security Fixes Highlight */}
              <Card className="border-red-500/30 bg-red-500/5 md:col-span-2">
                <CardHeader>
                  <CardTitle className="text-base text-red-400 flex items-center gap-2">
                    <Shield className="w-4 h-4" />
                    Security Vulnerabilities Fixed
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {deps
                      .filter((d) => d.cveIds.length > 0 || d.status === "critical")
                      .map((d) => (
                        <div
                          key={d.id}
                          className="flex items-start gap-3 p-3 rounded-lg bg-background/50 border border-red-500/20"
                          data-testid={`security-${d.id}`}
                        >
                          <Shield className="w-4 h-4 text-red-400 flex-shrink-0 mt-0.5" />
                          <div>
                            <p className="text-sm font-medium">{d.name}</p>
                            <p className="text-xs text-muted-foreground">{d.reason}</p>
                            {d.cveIds.map((cve) => (
                              <Badge key={cve} variant="destructive" className="mt-1 text-xs mr-1">
                                {cve}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      ))}
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* ── Code Changes Tab ─────────────────── */}
          <TabsContent value="code" className="mt-6 space-y-4">
            <p className="text-sm text-muted-foreground">
              Required source-level changes to migrate the Java codebase to modern APIs.
            </p>
            {(analysisData?.codeChanges ?? []).map((change, idx) => (
              <Card key={idx} className="border-border" data-testid={`code-change-${idx}`}>
                <CardHeader className="pb-2">
                  <div className="flex items-start justify-between gap-3 flex-wrap">
                    <CardTitle className="text-sm font-mono text-primary break-all">
                      {change.file}
                    </CardTitle>
                    <EffortBadge effort={change.effort} />
                  </div>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {change.changes.map((c, ci) => (
                      <li key={ci} className="flex items-start gap-2 text-sm">
                        <ChevronRight className="w-4 h-4 text-primary flex-shrink-0 mt-0.5" />
                        <span className="text-foreground/80">{c}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            ))}
          </TabsContent>

          {/* ── Plugins Tab ──────────────────────── */}
          <TabsContent value="plugins" className="mt-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Removed Plugins */}
              <div className="space-y-3">
                <h2 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                  <Trash2 className="w-4 h-4 text-slate-400" />
                  Removed Plugins
                </h2>
                {(analysisData?.plugins?.removed ?? []).map((p, i) => (
                  <Card key={i} className="border-slate-500/20 bg-slate-500/5" data-testid={`plugin-removed-${i}`}>
                    <CardContent className="p-4">
                      <p className="text-sm font-mono text-slate-300 break-all">{p.plugin}</p>
                      <p className="text-xs text-muted-foreground mt-1">{p.reason}</p>
                    </CardContent>
                  </Card>
                ))}
              </div>

              {/* Added Plugins */}
              <div className="space-y-3">
                <h2 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
                  <PlusCircle className="w-4 h-4 text-green-400" />
                  Added Plugins
                </h2>
                {(analysisData?.plugins?.added ?? []).map((p, i) => (
                  <Card key={i} className="border-green-500/20 bg-green-500/5" data-testid={`plugin-added-${i}`}>
                    <CardContent className="p-4">
                      <p className="text-sm font-mono text-green-300 break-all">{p.plugin}</p>
                      <p className="text-xs text-muted-foreground mt-1">{p.reason}</p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>

            {/* Migration quick-ref */}
            <Card className="mt-6 border-primary/20 bg-primary/5">
              <CardHeader>
                <CardTitle className="text-sm flex items-center gap-2">
                  <Zap className="w-4 h-4 text-primary" />
                  Quick Migration Commands
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {[
                  { cmd: "mvn versions:display-dependency-updates", desc: "Check for newer dependency versions" },
                  { cmd: "mvn dependency-check:check",              desc: "Run OWASP CVE vulnerability scan" },
                  { cmd: "mvn spring-boot:run",                     desc: "Run the modernised application" },
                  { cmd: "mvn clean package -DskipTests",           desc: "Build executable fat-JAR" },
                  { cmd: "find src -name '*.java' | xargs sed -i 's/javax\\.servlet/jakarta.servlet/g'", desc: "Auto-rename javax→jakarta (servlet)" },
                  { cmd: "find src -name '*.java' | xargs sed -i 's/import org\\.apache\\.log4j/import org.slf4j/g'", desc: "Auto-rename log4j→SLF4J imports" },
                ].map(({ cmd, desc }, i) => (
                  <div key={i} className="flex flex-col sm:flex-row sm:items-center gap-2 p-3 bg-background/60 rounded-lg border border-border">
                    <code className="text-xs font-mono text-primary flex-1 break-all">{cmd}</code>
                    <span className="text-xs text-muted-foreground sm:text-right sm:w-48 flex-shrink-0">{desc}</span>
                  </div>
                ))}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>

      {/* ── Footer ────────────────────────────────── */}
      <footer className="mt-16 border-t border-border py-6 text-center text-xs text-muted-foreground">
        Fusion · com.projectgoth · Java 1.5 → 21 modernisation analysis
      </footer>
    </div>
  );
}
