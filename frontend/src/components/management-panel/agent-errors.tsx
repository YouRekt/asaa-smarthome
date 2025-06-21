import AgentError from "@/components/management-panel/agent-error";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { AgentError as AgentErrorType } from "@/hooks/use-store";
import { parseTimestamp } from "@/lib/utils";
import { AlertTriangle } from "lucide-react";

const AgentErrors = ({ errors }: { errors: AgentErrorType[] }) => {
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
						<AgentError key={index} error={error} index={index} />
					))}
			</div>
		</ScrollArea>
	);
};

export default AgentErrors;
