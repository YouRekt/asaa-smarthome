import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { AgentError } from "@/hooks/use-store";
import { parseTimestamp } from "@/lib/utils";
import { AlertTriangle } from "lucide-react";

const AgentErrors = ({ errors }: { errors: AgentError[] }) => {
	if (errors.length === 0) {
		return (
			<div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
				<AlertTriangle className="h-12 w-12 mb-4 opacity-50" />
				<p className="text-lg font-medium">No errors</p>
				<p className="text-sm">
					This agent hasn't encountered any errors yet.
				</p>
			</div>
		);
	}

	return (
		<ScrollArea className="h-96">
			<div className="space-y-4 p-4">
				{errors
					.sort(
						(a, b) =>
							parseTimestamp(b.timestamp).getTime() -
							parseTimestamp(a.timestamp).getTime()
					)
					.map((error, index) => (
						<div
							key={index}
							className="p-3 mr-4 rounded-lg border bg-red-50 border-red-200 dark:bg-red-950/20 dark:border-red-800"
						>
							{/* Header */}
							<div className="flex items-center justify-between mb-2">
								<div className="flex items-center gap-2">
									<AlertTriangle className="h-4 w-4 text-red-500" />
									<Badge className="bg-red-500/20 text-red-800 dark:text-red-400 text-xs">
										ERROR
									</Badge>
								</div>
								<span className="text-xs text-muted-foreground">
									{error.timestamp}
								</span>
							</div>

							{/* Error message */}
							<p className="text-sm text-red-800 dark:text-red-200">
								{error.message}
							</p>
						</div>
					))}
			</div>
		</ScrollArea>
	);
};

export default AgentErrors;
