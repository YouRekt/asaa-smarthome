import clsx from "clsx";
import type { Agent as AgentType } from "@/hooks/use-store";
import {
	Tooltip,
	TooltipContent,
	TooltipProvider,
	TooltipTrigger,
} from "@/components/ui/tooltip";
import useStore from "@/hooks/use-store";
import { useEffect, useState, useRef } from "react";

export const Agent = ({ agent }: { agent: AgentType }) => {
	const { agentMessages } = useStore();
	const [open, setOpen] = useState(false);
	const timeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

	useEffect(() => {
		if (agentMessages[agent.aid]) {
			setOpen(true);

			// Clear any existing timeout
			if (timeoutRef.current) {
				clearTimeout(timeoutRef.current);
			}

			// Set new timeout
			timeoutRef.current = setTimeout(() => {
				setOpen(false);
			}, 3000);
		}

		return () => {
			if (timeoutRef.current) {
				clearTimeout(timeoutRef.current);
			}
		};
	}, [agentMessages, agent.aid]);

	return (
		<TooltipProvider>
			<Tooltip open={open}>
				<TooltipTrigger>
					<div
						className={clsx(
							"flex flex-col gap-2 h-fit p-2 rounded-md text-white",
							agent.name.includes("Sensor")
								? "bg-zinc-600 hover:bg-zinc-700"
								: "bg-blue-800 hover:bg-blue-900"
						)}
					>
						<h1 className="text-2xl font-bold">{agent.name}</h1>
						<p className="text-sm">{agent.area}</p>
					</div>
				</TooltipTrigger>
				<TooltipContent side="right" className="relative">
					{agentMessages[agent.aid]
						? `[${agentMessages[agent.aid].timestamp}] : ${
								agentMessages[agent.aid].message
						  }`
						: "No messages yet"}
					<div className="absolute -top-1 -right-1 w-3 h-3 bg-green-700 rounded-full animate-ping" />
				</TooltipContent>
			</Tooltip>
		</TooltipProvider>
	);
};
