import { Badge } from "@/components/ui/badge";
import type { AgentError as AgentErrorType } from "@/hooks/use-store";
import { AlertTriangle, CheckCircle } from "lucide-react";

interface AgentErrorProps {
	error: AgentErrorType;
	index: number;
}

const AgentError = ({ error, index }: AgentErrorProps) => {
	return (
		<div
			key={index}
			className={`p-3 mr-4 rounded-lg border ${
				error.resolved
					? "bg-orange-50 border-orange-200 dark:bg-orange-950/20 dark:border-orange-800"
					: "bg-red-50 border-red-200 dark:bg-red-950/20 dark:border-red-800"
			}`}
		>
			{/* Header */}
			<div className="flex items-center justify-between mb-2">
				<div className="flex items-center gap-2">
					{error.resolved ? (
						<CheckCircle className="h-4 w-4 text-orange-500" />
					) : (
						<AlertTriangle className="h-4 w-4 text-red-500" />
					)}
					<Badge
						className={`text-xs ${
							error.resolved
								? "bg-orange-500/20 text-orange-800 dark:text-orange-400"
								: "bg-red-500/20 text-red-800 dark:text-red-400"
						}`}
					>
						{error.resolved ? "RESOLVED" : "ERROR"}
					</Badge>
				</div>
				<span className="text-xs text-muted-foreground">
					{error.timestamp}
				</span>
			</div>

			{/* Error message */}
			<p
				className={`text-sm ${
					error.resolved
						? "text-orange-800 dark:text-orange-200"
						: "text-red-800 dark:text-red-200"
				}`}
			>
				{error.message}
			</p>
		</div>
	);
};

export default AgentError;
